// src/main/java/com/ks/tocho5/service/db/TeamService.java
package com.ks.tocho5.service.db;

import com.ks.tocho5.controller.Controller.CreateTeamRequest;
import com.ks.tocho5.controller.Controller.UpdateTeamRequest;
import com.ks.tocho5.model.AppUser;
import com.ks.tocho5.model.EquiposModel;
import com.ks.tocho5.model.EquiposStatsModel;
import com.ks.tocho5.model.TeamSearchProjection;
import com.ks.tocho5.repository.EquiposRepository;
import com.ks.tocho5.repository.EquiposFiltroRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class TeamService {

    private final AppUserService appUserService;
    private final EquiposRepository equiposRepository;
    private final EquiposFiltroRepository equiposFiltroRepository;

    public TeamService(AppUserService appUserService,
                       EquiposRepository equiposRepository,
                       EquiposFiltroRepository equiposFiltroRepository) {
        this.appUserService = appUserService;
        this.equiposRepository = equiposRepository;
        this.equiposFiltroRepository = equiposFiltroRepository;
    }

    private AppUser getCurrentUser(Jwt jwt) {
        return appUserService.syncFromJwt(jwt);
    }

    // opcional: versión vieja para compatibilidad


    @Transactional
    public EquiposModel createTeamForCurrentUser(Jwt jwt, CreateTeamRequest req) {
        AppUser user = getCurrentUser(jwt);

        if (!user.hasCaptainPrivileges()) {
            throw new RuntimeException("Solo capitanes o admins pueden crear equipos");
        }

        int currentTeams = equiposRepository.countByCaptainAndIsActiveTrue(user);
        if (currentTeams >= user.getMaxTeamsAllowed()) {
            throw new RuntimeException("Ya alcanzaste tu límite de equipos (" + user.getMaxTeamsAllowed() + ")");
        }

        // 1) Crear equipo
        EquiposModel team = new EquiposModel();
        team.setName(req.name());
        team.setColorPrimary(req.colorPrimary());
        team.setColorSecondary(req.colorSecondary());
        team.setCaptain(user);

        if (req.leagueId() != null) {
            // Asegúrate de que EquiposModel tenga este campo
            team.setLeagueId(req.leagueId());
        }

        team = equiposRepository.save(team);

        // 2) Crear inscripción en team_enrollment / equipos_stats
        if (req.seasonId() != null && req.categoryId() != null) {
            EquiposStatsModel enrollment = new EquiposStatsModel();
            enrollment.setTeam_id(team.getTeamId());
            enrollment.setSeason_id(req.seasonId());
            enrollment.setCategory_id(req.categoryId());
            equiposFiltroRepository.save(enrollment);
        }

        return team;
    }

    /**
     * Actualizar nombre y shortName de un equipo del usuario actual.
     */
    @Transactional
    public EquiposModel updateTeamForCurrentUser(Jwt jwt, Long teamId, UpdateTeamRequest req) {
        AppUser user = getCurrentUser(jwt);

        EquiposModel team = equiposRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        // Solo admin o capitán de ese equipo
        if (!user.isAdmin() &&
            (team.getCaptain() == null || !team.getCaptain().getId().equals(user.getId()))) {
            throw new RuntimeException("No puedes editar un equipo que no es tuyo");
        }

        if (req.name() == null || req.name().isBlank()) {
            throw new IllegalArgumentException("El nombre del equipo es obligatorio");
        }

        team.setName(req.name().trim());

        if (req.shortName() != null && !req.shortName().isBlank()) {
            team.setShortName(req.shortName().trim());
        } else {
            team.setShortName(null);
        }
        
     // Colores (solo si vienen)
        if (req.colorPrimary() != null && !req.colorPrimary().isBlank()) {
            String cp = req.colorPrimary().trim();
            if (!Objects.equals(team.getColorPrimary(), cp)) {
                team.setColorPrimary(cp);
            }
        }

        if (req.colorSecondary() != null && !req.colorSecondary().isBlank()) {
            String cs = req.colorSecondary().trim();
            if (!Objects.equals(team.getColorSecondary(), cs)) {
                team.setColorSecondary(cs);
            }
        }

        return equiposRepository.save(team);
    }

    /**
     * NUEVO:
     * Buscar equipos filtrando por leagueId, categoryCode y gender (rama).
     *
     * Por ahora esta implementación solo devuelve todos los equipos
     * para no romper nada. Luego podemos meter aquí un query real usando
     * EquiposFiltroRepository o EquiposRepository con joins a category.
     */
    @Transactional(readOnly = true)
    public List<EquiposModel> findTeamsFiltered(
            Integer leagueId,
            String categoryCode,
            String gender
    ) {
        // TODO: implementar filtro real (leagueId/categoryCode/gender) con tus tablas
        // por ahora se regresa todo para mantener el comportamiento actual
    	 return equiposRepository.findActiveTeamsFiltered(leagueId, categoryCode, gender);
    }

    @Transactional(readOnly = true)
    public List<TeamSearchProjection> searchTeamsForAgent(
            String query,
            Integer leagueId,
            String categoryCode,
            String gender,
            Integer limit
    ) {
        String normalizedQuery = normalizeSearchQuery(query);
        if (normalizedQuery == null) {
            return List.of();
        }

        return equiposRepository.searchActiveTeamsByName(
                normalizedQuery,
                leagueId,
                categoryCode,
                gender,
                clampSearchLimit(limit)
        );
    }

    private String normalizeSearchQuery(String query) {
        if (query == null) return null;

        String value = query.trim();
        return value.isEmpty() ? null : value;
    }

    private int clampSearchLimit(Integer limit) {
        if (limit == null) return 10;
        if (limit < 1) return 1;
        return Math.min(limit, 25);
    }
    

@Transactional
public EquiposModel setTeamActive(Jwt jwt, Integer teamId, boolean isActive) {
    AppUser user = getCurrentUser(jwt);

    // ✅ Solo permitimos DESACTIVAR
    if (isActive) {
        throw new RuntimeException("No se permite activar equipos por este endpoint. Solo desactivar (isActive=false).");
    }

    EquiposModel team = equiposRepository.findById(teamId.longValue())
            .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

    boolean isAdmin = user.isAdmin();

    // 👉 Esto es "isCaptainOwner": el usuario actual es el capitán de ESE equipo
    boolean isCaptainOwner =
            team.getCaptain() != null
            && team.getCaptain().getId() != null
            && Objects.equals(team.getCaptain().getId(), user.getId());

    // ✅ Admin o Capitán dueño pueden DESACTIVAR
    if (!isAdmin && !isCaptainOwner) {
        throw new RuntimeException("No tienes permisos para desactivar este equipo");
    }

    // (opcional) si ya está false, lo dejas igual (idempotente)
    team.setIsActive(false);
    return equiposRepository.save(team);
}
}
