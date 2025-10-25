package com.ks.tocho5.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.ks.tocho5.model.EquiposStatsDTO;
import com.ks.tocho5.model.GameModel;
import com.ks.tocho5.model.TeamStatsFilterDTO;
import com.ks.tocho5.model.DatoValorDTO;
import com.ks.tocho5.model.EquiposModel;
import com.ks.tocho5.repository.EquiposRepository;
import com.ks.tocho5.service.db.EquipoStatsService;
import com.ks.tocho5.service.db.GameService;

@RestController
@RequestMapping("/api")
public class Controller {

  private final EquiposRepository repository;
  private final EquipoStatsService service;
  private final GameService gameservice;

  public Controller(EquiposRepository repository, EquipoStatsService service, GameService gameservice) {
    this.repository = repository;
	this.service = service;
	this.gameservice = gameservice;
  }

  @GetMapping("/teams")
  public List<EquiposModel> findAll() {
    return repository.findAll();
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
