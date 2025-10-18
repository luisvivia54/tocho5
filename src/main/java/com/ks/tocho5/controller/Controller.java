package com.ks.tocho5.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.ks.tocho5.model.EquiposStatsDTO;
import com.ks.tocho5.model.TeamStatsFilterDTO;
import com.ks.tocho5.model.EquiposModel;
import com.ks.tocho5.repository.EquiposRepository;
import com.ks.tocho5.service.db.EquipoStatsService;

@RestController
@RequestMapping("/api")
public class Controller {

  private final EquiposRepository repository;
  private final EquipoStatsService service;

  public Controller(EquiposRepository repository, EquipoStatsService service) {
    this.repository = repository;
	this.service = service;
  }

  @GetMapping("/teams")
  public List<EquiposModel> findAll() {
    return repository.findAll();
  }
  @PostMapping("/search")
  public ResponseEntity<Page<EquiposStatsDTO>> search(@RequestBody TeamStatsFilterDTO f) {
    return ResponseEntity.ok(service.search(f));
  }
  
}
