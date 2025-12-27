// src/main/java/com/ks/tocho5/service/db/TeamService.java
package com.ks.tocho5.service.db;

import com.ks.tocho5.controller.Controller.CreateTeamRequest;
import com.ks.tocho5.controller.Controller.UpdateTeamRequest;
import com.ks.tocho5.model.AppUser;
import com.ks.tocho5.model.EquiposModel;
import com.ks.tocho5.model.EquiposStatsModel;
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
    public EquiposModel createTeamForCurrentUser(Jwt jwt, String name) {
        CreateTeamRequest req = new CreateTeamRequest(name, null, null, null);
        return createTeamForCurrentUser(jwt, req);
    }

    @Transactional
    public EquiposModel createTeamForCurrentUser(Jwt jwt, CreateTeamRequest req) {
        AppUser user = getCurrentUser(jwt);

        if (!user.hasCaptainPrivileges()) {
            throw new RuntimeException("Solo capitanes o admins pueden crear equipos");
        }

        int currentTeams = equiposRepository.countByCaptain(user);
        if (currentTeams >= user.getMaxTeamsAllowed()) {
            throw new RuntimeException("Ya alcanzaste tu límite de equipos (" + user.getMaxTeamsAllowed() + ")");
        }

        // 1) Crear equipo
        EquiposModel team = new EquiposModel();
        team.setName(req.name());
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
        return equiposRepository.findAll();
    }
}
