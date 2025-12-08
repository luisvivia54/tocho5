package com.ks.tocho5.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.io.IOException;

import com.ks.tocho5.model.EquiposStatsDTO;
import com.ks.tocho5.model.GameModel;
import com.ks.tocho5.model.GameStatusModel;
import com.ks.tocho5.model.StandingTeamModel;
import com.ks.tocho5.model.TeamStatsFilterDTO;
import com.ks.tocho5.model.AppUser;
import com.ks.tocho5.model.EquiposModel;
import com.ks.tocho5.repository.EquiposRepository;
import com.ks.tocho5.repository.JuegoStatus;
import com.ks.tocho5.repository.StandingTeamRepository;
import com.ks.tocho5.service.db.AppUserService;
import com.ks.tocho5.service.db.EquipoStatsService;
import com.ks.tocho5.service.db.GameService;
import com.ks.tocho5.service.db.TeamService;

import com.ks.tocho5.service.db.R2StorageService;
import org.springframework.web.multipart.MultipartFile;

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

  public Controller(
      EquiposRepository repository,
      EquipoStatsService service,
      AppUserService userservice,
      GameService gameservice,
      JuegoStatus juegostat,
      StandingTeamRepository standingrepo,
      TeamService teamService,
      R2StorageService r2StorageService
  ) {
    this.repository = repository;
    this.service = service;
    this.gameservice = gameservice;
    this.juegostat = juegostat;
    this.standingrepo = standingrepo;
    this.userservice = userservice;
    this.teamService = teamService;
    this.r2StorageService = r2StorageService;
  }

  // ================= EQUIPOS / PARTIDOS / TABLA =================

  @GetMapping("/teams")
  public List<EquiposModel> findAll() {
    return repository.findAll();
  }

  @GetMapping("/games")
  public List<GameStatusModel> findAllGames() {
    return juegostat.findAllScheduledWithTeams();
  }

  @GetMapping("/gamesFinal")
  public List<GameStatusModel> findAllFinalGames() {
    var ultimos5 = juegostat.findFinalWithTeams("FINAL", PageRequest.of(0, 5));
    return ultimos5;
  }

  @GetMapping("/points")
  public List<StandingTeamModel> findTablePoints() {
    return standingrepo.findAllWithTeam();
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
			}else {
				return "Algo salio mal";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "error"+e;
		}
  }

  // ================= LÓGICA DE CAPITÁN / MI EQUIPO =================

  // DTO para crear equipo
  public record CreateTeamRequest(String name) {}

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
   * Devuelve el estado del usuario respecto a equipos:
   * - rol (USER/CAPTAIN/ADMIN)
   * - cuántos equipos tiene
   * - si puede crear otro
   * - lista de equipos donde él es capitán
   */
  @GetMapping("/teams/mine")
  public MyTeamSummary getMyTeam(@AuthenticationPrincipal Jwt jwt) {
      // sincroniza usuario desde el JWT (si no existe, lo crea)
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
   * Crea un equipo para el usuario actual (si es capitán/admin y no se pasa de su límite).
   */
  @PostMapping("/teams/mine")
  public EquiposModel createMyTeam(
          @AuthenticationPrincipal Jwt jwt,
          @RequestBody CreateTeamRequest request
  ) {
      return teamService.createTeamForCurrentUser(jwt, request.name());
  }
  
  record TeamLogoResponse(Long teamId, String logoUrl) {}

  @PostMapping("/teams/{teamId}/logo")
  public String uploadTeamLogo(
          @AuthenticationPrincipal Jwt jwt,
          @PathVariable Long teamId,
          @RequestParam("logo") MultipartFile logoFile
  ) throws IOException {

      // 1) Usuario actual (Keycloak → app_user)
      AppUser user = userservice.syncFromJwt(jwt);

      // 2) Buscar equipo
      EquiposModel team = repository.findById(teamId)
              .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

      // 3) Validar que sea capitán de ese equipo o admin
      if (!user.isAdmin() &&
          (team.getCaptain() == null || !team.getCaptain().getId().equals(user.getId()))) {
          throw new RuntimeException("No puedes cambiar el logo de un equipo que no es tuyo");
      }

      // 4) Subir archivo a R2
      String logoUrl = r2StorageService.uploadTeamLogo(teamId, logoFile);

      // 5) Guardar URL en la BD
      team.setLogoUrl(logoUrl); // o setPhotoUrl(...)
      repository.save(team);

      // 6) Devolver datos al front
      return "logo ";
  }
}
