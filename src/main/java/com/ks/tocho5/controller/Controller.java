package com.ks.tocho5.controller;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.ks.tocho5.model.EquiposModel;
import com.ks.tocho5.repository.EquiposRepository;

@RestController
@RequestMapping("/api")
public class Controller {

  private final EquiposRepository repository;

  public Controller(EquiposRepository repository) {
    this.repository = repository;
  }

  @GetMapping("/prueba")
  public List<EquiposModel> findAll() {
    return repository.findAll();
  }
}
