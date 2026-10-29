// src/main/java/com/ks/tocho5/service/db/TeamService.java
package com.ks.tocho5.service.db;

import com.ks.tocho5.controller.Controller.CreateTeamRequest;
import com.ks.tocho5.controller.Controller.UpdateTeamRequest;
import com.ks.tocho5.controller.Controller.AdminUpdateTeamRequest;
import com.ks.tocho5.model.CategoryModel;
import com.ks.tocho5.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import com.ks.tocho5.model.AppUser;
import com.ks.tocho5.model.EquiposModel;
import com.ks.tocho5.model.EquiposStatsModel;
import com.ks.tocho5.model.TeamSearchProjection;
import com.ks.tocho5.repository.EquiposRepository;
import com.ks.tocho5.repository.EquiposFiltroRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
public class TeamService {

    private final AppUserService appUserService;
    private final EquiposRepository equiposRepository;
    private final EquiposFiltroRepository equiposFiltroRepository;
    private final CategoryRepository categoryRepository;
    private final JdbcTemplate jdbc;

    public TeamService(AppUserService appUserService,
                       EquiposRepository equiposRepository,
                       EquiposFiltroRepository equiposFiltroRepository,
                       CategoryRepository categoryRepository,
                       JdbcTemplate jdbc) {
        this.appUserService = appUserService;
        this.equiposRepository = equiposRepository;
        this.equiposFiltroRepository = equiposFiltroRepository;
        this.categoryRepository = categoryRepository;
        this.jdbc = jdbc;
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
            // Solo el admin decide la division: un capitan siempre entra en la A.
            Integer categoryId = user.isAdmin()
                    ? req.categoryId()
                    : resolveDivisionForCaptain(req.categoryId());

            EquiposStatsModel enrollment = new EquiposStatsModel();
            enrollment.setTeam_id(team.getTeamId());
            enrollment.setSeason_id(req.seasonId());
            enrollment.setCategory_id(categoryId);
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

    // ================= DIVISIONES A/B =================

    /** "A" / "B" para ramas divididas; null cuando la categoria no tiene division ("0"). */
    private static String divisionOf(CategoryModel c) {
        String raw = c.getLevelOrder() == null ? "" : c.getLevelOrder().trim();
        if (raw.isEmpty() || "0".equals(raw)) return null;
        return raw.toUpperCase(Locale.ROOT);
    }

    /** Misma liga, mismo code y mismo genero: solo cambia la division. */
    private static boolean sameBranch(CategoryModel a, CategoryModel b) {
        return Objects.equals(a.getLeagueId(), b.getLeagueId())
                && a.getCode() != null && a.getCode().equalsIgnoreCase(b.getCode())
                && Objects.equals(
                        a.getGender() == null ? null : a.getGender().toUpperCase(Locale.ROOT),
                        b.getGender() == null ? null : b.getGender().toUpperCase(Locale.ROOT));
    }

    /**
     * Un capitan no puede inscribirse en la B: si la categoria elegida es una
     * division distinta de la A, se cambia por la A de esa misma rama.
     */
    private Integer resolveDivisionForCaptain(Integer categoryId) {
        CategoryModel chosen = categoryRepository.findById(categoryId.longValue())
                .orElseThrow(() -> new IllegalArgumentException("La categoría elegida no existe"));

        String division = divisionOf(chosen);
        if (division == null || "A".equals(division)) {
            return categoryId;
        }

        return categoryRepository.findAll().stream()
                .filter(c -> sameBranch(c, chosen) && "A".equals(divisionOf(c)))
                .map(c -> c.getId().intValue())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró la División A de esta categoría. Contacta a la liga."));
    }

    /**
     * Edicion de un equipo desde el panel admin: nombre, nombre corto, colores
     * y division (A/B) de su inscripcion actual.
     *
     * Reglas para cambiar de division:
     *  - solo entre divisiones de la misma rama (misma liga, code y genero);
     *  - no se permite si el equipo ya tiene partidos (no cancelados) en esa
     *    temporada, para no dejar partidos ni tabla en la division equivocada.
     */
    @Transactional
    public Map<String, Object> adminUpdateTeam(Long teamId, AdminUpdateTeamRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("Faltan los datos del equipo");
        }

        EquiposModel team = equiposRepository.findById(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipo no encontrado"));

        if (req.name() != null) {
            if (req.name().isBlank()) {
                throw new IllegalArgumentException("El nombre del equipo es obligatorio");
            }
            team.setName(req.name().trim());
        }

        if (req.shortName() != null) {
            team.setShortName(req.shortName().isBlank() ? null : req.shortName().trim());
        }

        if (req.colorPrimary() != null && !req.colorPrimary().isBlank()) {
            team.setColorPrimary(req.colorPrimary().trim());
        }

        if (req.colorSecondary() != null && !req.colorSecondary().isBlank()) {
            team.setColorSecondary(req.colorSecondary().trim());
        }

        team = equiposRepository.save(team);

        if (req.categoryId() != null) {
            changeDivision(team.getTeamId(), req.categoryId());
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("teamId", team.getTeamId());
        out.put("name", team.getName());
        out.put("shortName", team.getShortName());
        out.put("colorPrimary", team.getColorPrimary());
        out.put("colorSecondary", team.getColorSecondary());
        out.put("categoryId", currentEnrollment(team.getTeamId()).map(e -> e.get("category_id")).orElse(null));
        return out;
    }

    private java.util.Optional<Map<String, Object>> currentEnrollment(Integer teamId) {
        List<Map<String, Object>> rows = jdbc.queryForList("""
            SELECT enrollment_id, season_id, category_id
            FROM public.team_enrollment
            WHERE team_id = ?
            ORDER BY season_id DESC
            LIMIT 1
        """, teamId);
        return rows.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(rows.get(0));
    }

    private void changeDivision(Integer teamId, Integer newCategoryId) {
        Map<String, Object> enrollment = currentEnrollment(teamId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El equipo no tiene inscripción, no se le puede asignar división"));

        Integer enrollmentId = ((Number) enrollment.get("enrollment_id")).intValue();
        Integer seasonId = ((Number) enrollment.get("season_id")).intValue();
        Integer oldCategoryId = ((Number) enrollment.get("category_id")).intValue();

        if (oldCategoryId.equals(newCategoryId)) {
            return; // sin cambio
        }

        CategoryModel oldCat = categoryRepository.findById(oldCategoryId.longValue())
                .orElseThrow(() -> new IllegalArgumentException("La categoría actual del equipo no existe"));
        CategoryModel newCat = categoryRepository.findById(newCategoryId.longValue())
                .orElseThrow(() -> new IllegalArgumentException("La división elegida no existe"));

        if (!sameBranch(oldCat, newCat)) {
            throw new IllegalArgumentException(
                    "Solo se puede mover al equipo entre divisiones de su misma categoría");
        }

        Integer games = jdbc.queryForObject("""
            SELECT COUNT(*)
            FROM public.game
            WHERE season_id = ?
              AND (home_team_id = ? OR away_team_id = ?)
              AND COALESCE(UPPER(status), '') <> 'CANCELLED'
        """, Integer.class, seasonId, teamId, teamId);

        if (games != null && games > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede cambiar de división: el equipo ya tiene " + games
                            + (games == 1 ? " partido" : " partidos")
                            + " en esta temporada. Bórralos primero o haz el cambio en la siguiente temporada.");
        }

        jdbc.update("""
            UPDATE public.team_enrollment
               SET category_id = ?
             WHERE enrollment_id = ?
        """, newCategoryId, enrollmentId);

        // Su fila de la tabla (en ceros, porque no ha jugado) se mueve con el equipo.
        int moved = jdbc.update("""
            UPDATE public.standing_team
               SET category_id = ?
             WHERE season_id = ?
               AND team_id = ?
               AND category_id = ?
        """, newCategoryId, seasonId, teamId, oldCategoryId);

        if (moved == 0) {
            jdbc.update("""
                INSERT INTO public.standing_team
                  (season_id, category_id, team_id, gp, wins, losses, draws,
                   points_for, points_against, table_points)
                VALUES (?, ?, ?, 0, 0, 0, 0, 0, 0, 0)
                ON CONFLICT (season_id, category_id, team_id) DO NOTHING
            """, seasonId, newCategoryId, teamId);
        }
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
