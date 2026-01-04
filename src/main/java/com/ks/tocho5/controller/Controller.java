// src/main/java/com/ks/tocho5/controller/Controller.java
package com.ks.tocho5.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.ks.tocho5.model.*;
import com.ks.tocho5.model.TeamDetailDTOs.TeamDetailDTO;

import com.ks.tocho5.model.CategoryDto;
import com.ks.tocho5.model.TeamListProjection;

import com.ks.tocho5.model.PlayerGameStatsUpsertDTO;
import com.ks.tocho5.model.PlayerSeasonStatsDTO;

import com.ks.tocho5.repository.EquiposRepository;
import com.ks.tocho5.repository.JuegoStatus;
import com.ks.tocho5.repository.JuegosRepository;
import com.ks.tocho5.repository.StandingTeamRepository;

import com.ks.tocho5.service.db.AppUserService;
import com.ks.tocho5.service.db.CategoryService;
import com.ks.tocho5.service.db.EquipoStatsService;
import com.ks.tocho5.service.db.GameService;
import com.ks.tocho5.service.db.PlayerService;
import com.ks.tocho5.service.db.R2StorageService;
import com.ks.tocho5.service.db.TeamCarouselService;
import com.ks.tocho5.service.db.TeamDetailService;
import com.ks.tocho5.service.db.TeamService;
import com.ks.tocho5.service.db.PlayerStatsService;

// ✅ nuevo (admin users)
import com.ks.tocho5.service.db.AdminUserService;
import com.ks.tocho5.model.admin.AdminUserPatchRequest;
import com.ks.tocho5.model.admin.AdminUserRowDTO;

@RestController
@RequestMapping("/api")
public class Controller {

    private final EquiposRepository repository;
    private final EquipoStatsService service;
    private final GameService gameservice;
    private final JuegoStatus juegostat;
    private final StandingTeamRepository standingrepo;
    private final AppUserService userservice;
    private final TeamService teamService;
    private final R2StorageService r2StorageService;
    private final PlayerService playerService;
    private final TeamDetailService teamDetailService;
    private final TeamCarouselService teamCarouselService;
    private final CategoryService categoryService;
    private final JuegosRepository juegosrepo;
    private final PlayerStatsService playerStatsService;

    // ✅ nuevo
    private final AdminUserService adminUserService;

    private final ObjectMapper objectMapper;

    public Controller(
            EquiposRepository repository,
            EquipoStatsService service,
            AppUserService userservice,
            GameService gameservice,
            JuegoStatus juegostat,
            JuegosRepository juegosrepo,
            StandingTeamRepository standingrepo,
            TeamService teamService,
            R2StorageService r2StorageService,
            PlayerService playerService,
            TeamDetailService teamDetailService,
            TeamCarouselService teamCarouselService,
            CategoryService categoryService,
            PlayerStatsService playerStatsService,
            // ✅ nuevo
            AdminUserService adminUserService,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.service = service;
        this.userservice = userservice;
        this.gameservice = gameservice;
        this.juegostat = juegostat;
        this.juegosrepo = juegosrepo;
        this.standingrepo = standingrepo;
        this.teamService = teamService;
        this.r2StorageService = r2StorageService;
        this.playerService = playerService;
        this.teamDetailService = teamDetailService;
        this.teamCarouselService = teamCarouselService;
        this.categoryService = categoryService;
        this.playerStatsService = playerStatsService;

        this.adminUserService = adminUserService;

        this.objectMapper = objectMapper;
    }

    // ================= EQUIPOS =================

    @GetMapping("/teams")
    public List<EquiposModel> findAllTeams(
            @RequestParam(name = "leagueId", required = false) Integer leagueId,
            @RequestParam(name = "categoryCode", required = false) String categoryCode,
            @RequestParam(name = "gender", required = false) String gender
    ) {
        String code = normalizeFilterParam(categoryCode);
        String gen  = normalizeFilterParam(gender);

        if (leagueId == null && code == null && gen == null) {
            return repository.findByIsActiveTrueOrderByNameAsc();
        }

        return teamService.findTeamsFiltered(leagueId, code, gen);
    }

    @GetMapping("/teams/list")
    public List<TeamListProjection> listTeamsWithEnrollment(
            @RequestParam(name = "leagueId", required = false) Integer leagueId,
            @RequestParam(name = "categoryCode", required = false) String categoryCode,
            @RequestParam(name = "gender", required = false) String gender
    ) {
        String code = normalizeFilterParam(categoryCode);
        String gen  = normalizeFilterParam(gender);
        return repository.findTeamsList(leagueId, code, gen);
    }

    // ================= PARTIDOS =================

    @GetMapping("/games")
    public List<GameStatusModel> findAllGames(
            @RequestParam(name = "leagueId", required = false) Integer leagueId,
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "gender", required = false) String gender,
            @RequestParam(name = "roundLabel", required = false) String roundLabel
    ) {
        String c = normalizeFilterParam(code);
        String g = normalizeFilterParam(gender);
        String r = normalizeFilterParam(roundLabel);

        if (leagueId == null && c == null && g == null && r == null) {
            return juegostat.findAllScheduledWithTeams();
        }
        return juegostat.findScheduledWithTeamsFiltered(c, g, r);
    }

    @GetMapping("/gamesFinal")
    public List<GameStatusModel> findAllFinalGames(
            @RequestParam(name = "leagueId", required = false) Integer leagueId,
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "gender", required = false) String gender,
            @RequestParam(name = "roundLabel", required = false) String roundLabel,
            @RequestParam(name = "size", required = false, defaultValue = "500") int size,
            @RequestParam(name = "all", required = false, defaultValue = "false") boolean all
    ) {
        String c = normalizeFilterParam(code);
        String g = normalizeFilterParam(gender);
        String r = normalizeFilterParam(roundLabel);

        boolean hasFilters = (leagueId != null || c != null || g != null || r != null);

        List<GameStatusModel> finals = (all || hasFilters)
                ? juegostat.findFinalWithTeamsFiltered(c, g, r)
                : juegostat.findFinalWithTeams("FINAL", PageRequest.of(0, size));

        if (finals == null || finals.isEmpty()) return finals;

        List<Integer> gameIds = finals.stream()
                .map(GameStatusModel::getGame_id)
                .filter(java.util.Objects::nonNull)
                .toList();

        if (gameIds.isEmpty()) return finals;

        List<GameModel> scores = juegosrepo.findAllById(gameIds);

        java.util.Map<Integer, GameModel> scoreByGameId = scores.stream()
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toMap(
                        GameModel::getGame_id,
                        java.util.function.Function.identity(),
                        (a, b) -> a
                ));

        for (GameStatusModel gm : finals) {
            GameModel sc = scoreByGameId.get(gm.getGame_id());
            gm.setHomeScore(sc != null ? sc.getHome_score() : null);
            gm.setAwayScore(sc != null ? sc.getAway_score() : null);
        }

        return finals;
    }

    // ================= TABLA (POINTS) =================

    @GetMapping("/points")
    public List<PointsRowProjection> findTablePoints(
            @RequestParam(name = "leagueId", required = false) Integer leagueId,
            @RequestParam(name = "categoryCode", required = false) String categoryCode,
            @RequestParam(name = "gender", required = false) String gender
    ) {
        String code = normalizeFilterParam(categoryCode);
        String gen  = normalizeFilterParam(gender);
        return standingrepo.findPointsList(leagueId, code, gen);
    }

    @PostMapping("/search")
    public ResponseEntity<Page<EquiposStatsDTO>> search(@RequestBody TeamStatsFilterDTO f) {
        return ResponseEntity.ok(service.search(f));
    }

    // ================= USUARIO =================

    @GetMapping("/me")
    public String me(@AuthenticationPrincipal Jwt jwt) {
        AppUser user = userservice.syncFromJwt(jwt);
        return "Hola " + user.getFullName() + " (id interno=" + user.getId() + ")";
    }

    // ================= ADMIN / USERS =================
    // GET /api/admin/users?q=&role=&active=&page=&size=&sort=
    // sort: createdDesc | createdAsc | nameAsc | teamsDesc
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('admin')")
    public Page<AdminUserRowDTO> adminListUsers(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "role", required = false) String role,
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "25") int size,
            @RequestParam(name = "sort", required = false, defaultValue = "createdDesc") String sort
    ) {
        return adminUserService.list(q, role, active, page, size, sort);
    }

    // PATCH /api/admin/users/{id}
    // body: { "role": "USER"|"CAPTAIN", "maxTeamsAllowed": 0..99, "isActive": true|false }
    @PatchMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('admin')")
    public AdminUserRowDTO adminPatchUser(
            @PathVariable Long id,
            @RequestBody AdminUserPatchRequest req
    ) {
        return adminUserService.patch(id, req);
    }

    // ================= ACTUALIZAR PARTIDO =================

    @PostMapping("/partido/update")
    public Object partidoupdate(@RequestBody JsonNode body) {
        try {
            if (body.isArray()) {
                List<GameModel> batch = objectMapper.convertValue(
                        body,
                        new TypeReference<List<GameModel>>() {}
                );
                return gameservice.saveGames(batch);
            }

            GameModel datosEntrada = objectMapper.convertValue(body, GameModel.class);
            String respSave = gameservice.saveGame(datosEntrada);
            if ("OK".equals(respSave)) return respSave;
            return "Algo salio mal";

        } catch (Exception e) {
            e.printStackTrace();
            return "error " + e.getMessage();
        }
    }

    // ================== ✅ STATS POR PARTIDO (UPSERT) ==================

    @PutMapping("/games/{gameId}/player-stats")
    public ResponseEntity<Void> upsertPlayerGameStats(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long gameId,
            @RequestBody List<PlayerGameStatsUpsertDTO> body
    ) {
        playerStatsService.upsertStatsForGame(jwt, gameId, body);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/players")
    public List<PlayerSeasonStatsDTO> getPlayerSeasonStats(
            @RequestParam(name = "leagueId") Long leagueId,
            @RequestParam(name = "seasonId", required = false) Long seasonId
    ) {
        return playerStatsService.getLeaderboard(leagueId, seasonId);
    }

    // ================= MI EQUIPO =================

    public record CreateTeamRequest(
            String name,
            Integer seasonId,
            Integer categoryId,
            Integer leagueId,
            String colorPrimary,
            String colorSecondary
    ) {}

    public record UpdateTeamRequest(
            String name,
            String shortName,
            String colorPrimary,
            String colorSecondary
    ) {}

    public record MyTeamSummary(
            Long userId,
            String role,
            Integer maxTeamsAllowed,
            Integer currentTeams,
            boolean hasCaptainPrivileges,
            boolean canCreateTeam,
            List<EquiposModel> teams
    ) {}

    @GetMapping("/teams/mine")
    public MyTeamSummary getMyTeam(@AuthenticationPrincipal Jwt jwt) {
        AppUser user = userservice.syncFromJwt(jwt);

        List<EquiposModel> teams = repository.findByCaptainAndIsActiveTrue(user);
        int currentTeams = teams.size();

        boolean hasCaptainPrivileges = user.hasCaptainPrivileges();
        boolean canCreateTeam = hasCaptainPrivileges && currentTeams < user.getMaxTeamsAllowed();

        return new MyTeamSummary(
                user.getId(),
                user.getRole(),
                user.getMaxTeamsAllowed(),
                currentTeams,
                hasCaptainPrivileges,
                canCreateTeam,
                teams
        );
    }

    @PostMapping("/teams/mine")
    public EquiposModel createMyTeam(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody CreateTeamRequest request
    ) {
        return teamService.createTeamForCurrentUser(jwt, request);
    }

    @GetMapping("/teams/{teamId}")
    public ResponseEntity<EquiposModel> getTeamById(@PathVariable Integer teamId) {
        return repository.findById(teamId.longValue())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/teams/{teamId}")
    public EquiposModel updateMyTeam(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer teamId,
            @RequestBody UpdateTeamRequest request
    ) {
        return teamService.updateTeamForCurrentUser(jwt, teamId.longValue(), request);
    }

    // ================= LOGO =================

    @PostMapping("/teams/{teamId}/logo")
    public String uploadTeamLogo(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer teamId,
            @RequestParam("logo") MultipartFile logoFile
    ) throws IOException {

        AppUser user = userservice.syncFromJwt(jwt);

        EquiposModel team = repository.findById(teamId.longValue())
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        if (!user.isAdmin() &&
                (team.getCaptain() == null || !team.getCaptain().getId().equals(user.getId()))) {
            throw new RuntimeException("No puedes cambiar el logo de un equipo que no es tuyo");
        }

        String logoUrl = r2StorageService.uploadTeamLogo(teamId.longValue(), logoFile);
        team.setLogoUrl(logoUrl);
        repository.save(team);

        return "logo";
    }

    // ================= JUGADORES =================

    @GetMapping("/teams/{teamId}/players")
    public List<PlayerModel> getPlayersByTeam(@PathVariable Integer teamId) {
        return playerService.getPlayersByTeam(teamId.longValue());
    }

    @PostMapping("/teams/{teamId}/players")
    public PlayerModel createPlayerForTeam(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer teamId,
            @RequestParam("fullName") String fullName,
            @RequestParam("curp") String curp,
            @RequestParam(value = "jerseyNumber", required = false) Integer jerseyNumber,
            @RequestParam(value = "birthdate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthdate,
            @RequestParam("photo") MultipartFile photo
    ) throws IOException {

        return playerService.createPlayerForTeam(
                jwt,
                teamId.longValue(),
                fullName,
                curp,
                jerseyNumber,
                birthdate,
                photo
        );
    }

    @PutMapping("/teams/{teamId}/players/{playerId}")
    public PlayerModel updatePlayer(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer teamId,
            @PathVariable Long playerId,
            @RequestParam("fullName") String fullName,
            @RequestParam("curp") String curp,
            @RequestParam(value = "jerseyNumber", required = false) Integer jerseyNumber,
            @RequestParam(value = "birthdate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthdate,
            @RequestParam(value = "photo", required = false) MultipartFile newPhoto
    ) throws IOException {

        return playerService.updatePlayer(
                jwt,
                teamId.longValue(),
                playerId,
                fullName,
                curp,
                jerseyNumber,
                birthdate,
                newPhoto
        );
    }

    @DeleteMapping("/teams/{teamId}/players/{playerId}")
    public ResponseEntity<Void> deletePlayer(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer teamId,
            @PathVariable Long playerId
    ) {
        playerService.deletePlayer(jwt, teamId.longValue(), playerId);
        return ResponseEntity.noContent().build();
    }

    // ================= FOTOS CARRUSEL =================

    @GetMapping("/teams/{teamId}/photos")
    public List<TeamPhotoModel> listTeamPhotos(@PathVariable Integer teamId) {
        return teamCarouselService.listPhotos(teamId.longValue());
    }

    @PostMapping("/teams/{teamId}/photos")
    public TeamPhotoModel addTeamPhoto(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer teamId,
            @RequestParam("photo") MultipartFile photo,
            @RequestParam(value = "positionIndex", required = false) Integer positionIndex
    ) throws IOException {
        return teamCarouselService.addPhoto(jwt, teamId.longValue(), photo, positionIndex);
    }

    @PutMapping("/teams/{teamId}/photos/{photoId}")
    public TeamPhotoModel updateTeamPhoto(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer teamId,
            @PathVariable Long photoId,
            @RequestParam("photo") MultipartFile photo,
            @RequestParam(value = "positionIndex", required = false) Integer positionIndex
    ) throws IOException {
        return teamCarouselService.updatePhoto(jwt, teamId.longValue(), photoId, photo, positionIndex);
    }

    @DeleteMapping("/teams/{teamId}/photos/{photoId}")
    public ResponseEntity<Void> deleteTeamPhoto(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer teamId,
            @PathVariable Long photoId
    ) throws IOException {
        teamCarouselService.deletePhoto(jwt, teamId.longValue(), photoId);
        return ResponseEntity.noContent().build();
    }

    // ================= DETALLE PÚBLICO =================

    @GetMapping("/teams/{teamId}/detail")
    public TeamDetailDTO getTeamDetail(@PathVariable Integer teamId) {
        return teamDetailService.getTeamDetail(teamId.longValue());
    }

    // ================= CATEGORÍAS =================

    @GetMapping("/categories")
    public List<CategoryDto> listCategories(
            @RequestParam(name = "leagueId", required = false) Long leagueId,
            @RequestParam(name = "gender", required = false) String gender
    ) {
        return categoryService.getCategories(leagueId, gender);
    }

    // ================= ACTIVAR / DESACTIVAR (ELIMINAR LÓGICO) =================

    @PatchMapping("/teams/{teamId}/active")
    public ResponseEntity<EquiposModel> setTeamActive(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer teamId,
            @RequestBody SetTeamActiveRequestDTO req
    ) {
        if (req == null || req.getIsActive() == null) {
            throw new IllegalArgumentException("Falta isActive (true/false)");
        }

        EquiposModel updated = teamService.setTeamActive(jwt, teamId, req.getIsActive());
        return ResponseEntity.ok(updated);
    }

    // ================= HELPERS =================

    private String normalizeFilterParam(String value) {
        if (value == null) return null;
        String v = value.trim();
        if (v.isEmpty() || "all".equalsIgnoreCase(v)) return null;
        return v.toUpperCase();
    }
}
