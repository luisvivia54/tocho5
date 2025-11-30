package com.ks.tocho5.controller;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.List;
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

@RestController
@RequestMapping("/api")
public class Controller {

  private final EquiposRepository repository;
  private final EquipoStatsService service;
  private final GameService gameservice;
  private final JuegoStatus juegostat;
  private final StandingTeamRepository standingrepo;
  private final AppUserService userservice;

  public Controller(EquiposRepository repository, EquipoStatsService service,AppUserService userservice, GameService gameservice, JuegoStatus juegostat,StandingTeamRepository standingrepo) {
    this.repository = repository;
	this.service = service;
	this.gameservice = gameservice;
	this.juegostat = juegostat;
	this.standingrepo = standingrepo;
	this.userservice = userservice;
  }

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
  @GetMapping("/me")
  public String me(@AuthenticationPrincipal Jwt jwt) {
      // Aquí se sincroniza (crea/actualiza) el usuario en Neon
      AppUser user = userservice.getOrCreateFromJwt(jwt);

      return "Hola " + (user.getFullName() != null ? user.getFullName() : user.getEmail())
              + " (id interno=" + user.getId() + ")";
  }
  
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
  
}
