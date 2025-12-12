package com.ks.tocho5.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import com.ks.tocho5.model.EquiposStatsDTO;
import com.ks.tocho5.model.GameModel;
import com.ks.tocho5.model.GameStatusModel;
import com.ks.tocho5.model.PlayerModel;
import com.ks.tocho5.model.StandingTeamModel;
import com.ks.tocho5.model.TeamStatsFilterDTO;
import com.ks.tocho5.model.AppUser;
import com.ks.tocho5.model.EquiposModel;
import com.ks.tocho5.model.TeamDetailDTOs.TeamDetailDTO;
import com.ks.tocho5.model.TeamPhotoModel;

import com.ks.tocho5.repository.EquiposRepository;
import com.ks.tocho5.repository.JuegoStatus;
import com.ks.tocho5.repository.StandingTeamRepository;

import com.ks.tocho5.service.db.AppUserService;
import com.ks.tocho5.service.db.EquipoStatsService;
import com.ks.tocho5.service.db.GameService;
import com.ks.tocho5.service.db.TeamService;
import com.ks.tocho5.service.db.PlayerService;
import com.ks.tocho5.service.db.R2StorageService;
import com.ks.tocho5.service.db.TeamDetailService;
import com.ks.tocho5.service.db.TeamCarouselService;
import com.ks.tocho5.service.db.CategoryService;
import com.ks.tocho5.model.CategoryDto;

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
    private final CategoryService categoryService; // NUEVO

    public Controller(
            EquiposRepository repository,
            EquipoStatsService service,
            AppUserService userservice,
            GameService gameservice,
            JuegoStatus juegostat,
            StandingTeamRepository standingrepo,
            TeamService teamService,
            R2StorageService r2StorageService,
            PlayerService playerService,
            TeamDetailService teamDetailService,
            TeamCarouselService teamCarouselService,
            CategoryService categoryService // NUEVO
    ) {
        this.repository = repository;
        this.service = service;
        this.gameservice = gameservice;
        this.juegostat = juegostat;
        this.standingrepo = standingrepo;
        this.userservice = userservice;
        this.teamService = teamService;
        this.r2StorageService = r2StorageService;
        this.playerService = playerService;
        this.teamDetailService = teamDetailService;
        this.teamCarouselService = teamCarouselService;
        this.categoryService = categoryService;
    }

    // ================= EQUIPOS / PARTIDOS / TABLA =================

    /**
     * GET /api/teams
     * - Sin parámetros -> todos los equipos (comportamiento viejo).
     * - Con leagueId / categoryCode / gender -> filtrado.
     */
    @GetMapping("/teams")
    public List<EquiposModel> findAllTeams(
            @RequestParam(name = "leagueId", required = false) Integer leagueId,
            @RequestParam(name = "categoryCode", required = false) String categoryCode,
            @RequestParam(name = "gender", required = false) String gender
    ) {
        String code = normalizeFilterParam(categoryCode);
        String gen = normalizeFilterParam(gender);

        // Sin filtros: regresamos todo como antes
        if (leagueId == null && code == null && gen == null) {
            return repository.findAll();
        }

        // Filtros: delegamos a TeamService (ahí puedes usar Category + joins)
        return teamService.findTeamsFiltered(leagueId, code, gen);
    }

    /**
     * GET /api/games
     * - Sin filtros -> todos los partidos programados (viejo comportamiento).
     * - Con leagueId / categoryCode / gender -> filtrados.
     */
    @GetMapping("/games")
    public List<GameStatusModel> findAllGames(
            @RequestParam(name = "leagueId", required = false) Integer leagueId,
            @RequestParam(name = "categoryCode", required = false) String categoryCode,
            @RequestParam(name = "gender", required = false) String gender
    ) {
        String code = normalizeFilterParam(categoryCode);
        String gen = normalizeFilterParam(gender);

        if (leagueId == null && code == null && gen == null) {
            return juegostat.findAllScheduledWithTeams();
        }

        return juegostat.findScheduledWithTeamsFiltered(leagueId, code, gen);
    }

    @GetMapping("/gamesFinal")
    public List<GameStatusModel> findAllFinalGames() {
        var ultimos5 = juegostat.findFinalWithTeams("FINAL", PageRequest.of(0, 5));
        return ultimos5;
    }

    /**
     * GET /api/points
     * - Sin filtros -> tabla completa (findAllWithTeam()).
     * - Con leagueId / categoryCode / gender -> filtrada.
     */
    @GetMapping("/points")
    public List<StandingTeamModel> findTablePoints(
            @RequestParam(name = "leagueId", required = false) Integer leagueId,
            @RequestParam(name = "categoryCode", required = false) String categoryCode,
            @RequestParam(name = "gender", required = false) String gender
    ) {
        String code = normalizeFilterParam(categoryCode);
        String gen = normalizeFilterParam(gender);

        if (leagueId == null && code == null && gen == null) {
            return standingrepo.findAllWithTeam();
        }

        return standingrepo.findAllWithTeamFiltered(leagueId, code, gen);
    }

    @PostMapping("/search")
    public ResponseEntity<Page<EquiposStatsDTO>> search(@RequestBody TeamStatsFilterDTO f) {
        return ResponseEntity.ok(service.search(f));
    }

    // ================= USUARIO (KEYCLOAK -> app_user) =================

    @GetMapping("/me")
    public String me(@AuthenticationPrincipal Jwt jwt) {
        AppUser user = userservice.syncFromJwt(jwt);
        return "Hola " + user.getFullName() + " (id interno=" + user.getId() + ")";
    }

    // ================= ACTUALIZAR PARTIDO =================

    @PostMapping("/partido/update")
    public String partidoupdate(@RequestBody GameModel datosEntrada) {
        try {
            String respSave = gameservice.saveGame(datosEntrada);
            if (respSave.equals("OK")) {
                return respSave;
            } else {
                return "Algo salio mal";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "error" + e;
        }
    }

    // ================= LÓGICA DE CAPITÁN / MI EQUIPO =================

    // DTO para crear equipo (con season/category/league)
    public record CreateTeamRequest(
            String name,
            Integer seasonId,
            Integer categoryId,
            Integer leagueId
    ) {}

    // DTO para actualizar equipo (nombre + shortName)
    public record UpdateTeamRequest(
            String name,
            String shortName
    ) {}

    // DTO para que el front pueda decidir qué botón mostrar
    public record MyTeamSummary(
            Long userId,
            String role,
            Integer maxTeamsAllowed,
            Integer currentTeams,
            boolean hasCaptainPrivileges,
            boolean canCreateTeam,
            List<EquiposModel> teams
    ) {}

    /**
     * GET /api/teams/mine
     */
    @GetMapping("/teams/mine")
    public MyTeamSummary getMyTeam(@AuthenticationPrincipal Jwt jwt) {
        AppUser user = userservice.syncFromJwt(jwt);

        List<EquiposModel> teams = repository.findByCaptain(user);
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

    /**
     * POST /api/teams/mine
     */
    @PostMapping("/teams/mine")
    public EquiposModel createMyTeam(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody CreateTeamRequest request
    ) {
        return teamService.createTeamForCurrentUser(jwt, request);
    }

    /**
     * PUT /api/teams/{teamId}
     */
    @PutMapping("/teams/{teamId}")
    public EquiposModel updateMyTeam(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long teamId,
            @RequestBody UpdateTeamRequest request
    ) {
        return teamService.updateTeamForCurrentUser(jwt, teamId, request);
    }

    @GetMapping("/teams/{teamId}")
    public ResponseEntity<EquiposModel> getTeamById(@PathVariable Long teamId) {
        return repository.findById(teamId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ================= LOGO DEL EQUIPO =================

    public record TeamLogoResponse(Long teamId, String logoUrl) {}

    @PostMapping("/teams/{teamId}/logo")
    public String uploadTeamLogo(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long teamId,
            @RequestParam("logo") MultipartFile logoFile
    ) throws IOException {

        AppUser user = userservice.syncFromJwt(jwt);

        EquiposModel team = repository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        if (!user.isAdmin() &&
            (team.getCaptain() == null || !team.getCaptain().getId().equals(user.getId()))) {
            throw new RuntimeException("No puedes cambiar el logo de un equipo que no es tuyo");
        }

        String logoUrl = r2StorageService.uploadTeamLogo(teamId, logoFile);

        team.setLogoUrl(logoUrl);
        repository.save(team);

        return "logo";
    }

    // ================ JUGADORES ===================

    @GetMapping("/teams/{teamId}/players")
    public List<PlayerModel> getPlayersByTeam(@PathVariable Long teamId) {
        return playerService.getPlayersByTeam(teamId);
    }

    @PostMapping("/teams/{teamId}/players")
    public PlayerModel createPlayerForTeam(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long teamId,
            @RequestParam("fullName") String fullName,
            @RequestParam("curp") String curp,
            @RequestParam(value = "jerseyNumber", required = false) Integer jerseyNumber,
            @RequestParam(value = "birthdate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthdate,
            @RequestParam("photo") MultipartFile photo
    ) throws IOException {

        return playerService.createPlayerForTeam(
                jwt,
                teamId,
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
            @PathVariable Long teamId,
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
                teamId,
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
            @PathVariable Long teamId,
            @PathVariable Long playerId
    ) {
        playerService.deletePlayer(jwt, teamId, playerId);
        return ResponseEntity.noContent().build();
    }

    // =============== CARRUSEL DE FOTOS DEL EQUIPO =================

    @GetMapping("/teams/{teamId}/photos")
    public List<TeamPhotoModel> listTeamPhotos(@PathVariable Long teamId) {
        return teamCarouselService.listPhotos(teamId);
    }

    @PostMapping("/teams/{teamId}/photos")
    public TeamPhotoModel addTeamPhoto(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long teamId,
            @RequestParam("photo") MultipartFile photo,
            @RequestParam(value = "positionIndex", required = false) Integer positionIndex
    ) throws IOException {

        return teamCarouselService.addPhoto(jwt, teamId, photo, positionIndex);
    }

    @PutMapping("/teams/{teamId}/photos/{photoId}")
    public TeamPhotoModel updateTeamPhoto(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long teamId,
            @PathVariable Long photoId,
            @RequestParam("photo") MultipartFile photo,
            @RequestParam(value = "positionIndex", required = false) Integer positionIndex
    ) throws IOException {

        return teamCarouselService.updatePhoto(jwt, teamId, photoId, photo, positionIndex);
    }

    @DeleteMapping("/teams/{teamId}/photos/{photoId}")
    public ResponseEntity<Void> deleteTeamPhoto(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long teamId,
            @PathVariable Long photoId
    ) throws IOException {

        teamCarouselService.deletePhoto(jwt, teamId, photoId);
        return ResponseEntity.noContent().build();
    }

    // =============== DETALLE PÚBLICO DE EQUIPO =================

    @GetMapping("/teams/{teamId}/detail")
    public TeamDetailDTO getTeamDetail(@PathVariable Long teamId) {
        return teamDetailService.getTeamDetail(teamId);
    }

    // =============== CATEGORÍAS (para filtros del front) =================

    @GetMapping("/categories")
    public List<CategoryDto> listCategories(
            @RequestParam(name = "leagueId", required = false) Long leagueId,
            @RequestParam(name = "gender", required = false) String gender
    ) {
        return categoryService.getCategories(leagueId, gender);
    }

    // =============== HELPERS PRIVADOS =================

    /**
     * Normaliza parámetros de filtro:
     * - null, "", "   ", "all" -> null
     * - cualquier otro valor se regresa tal cual (el repo se encarga de UPPER/LOWER)
     */
    private String normalizeFilterParam(String value) {
        if (value == null) return null;
        String v = value.trim();
        if (v.isEmpty() || "all".equalsIgnoreCase(v)) {
            return null;
        }
        return v;
    }
}
