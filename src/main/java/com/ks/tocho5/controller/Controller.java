package com.ks.tocho5.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.ks.tocho5.model.EquiposStatsDTO;
import com.ks.tocho5.model.GameModel;
import com.ks.tocho5.model.GameStatusModel;
import com.ks.tocho5.model.StandingTeamModel;
import com.ks.tocho5.model.TeamStatsFilterDTO;
import com.ks.tocho5.model.EquiposModel;
import com.ks.tocho5.repository.EquiposRepository;
import com.ks.tocho5.repository.JuegoStatus;
import com.ks.tocho5.repository.StandingTeamRepository;
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

  public Controller(EquiposRepository repository, EquipoStatsService service, GameService gameservice, JuegoStatus juegostat,StandingTeamRepository standingrepo) {
    this.repository = repository;
	this.service = service;
	this.gameservice = gameservice;
	this.juegostat = juegostat;
	this.standingrepo = standingrepo;
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
	  List<GameStatusModel> ultimos5 = juegostat.findFinalWithTeams(PageRequest.of(0, 5));
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
