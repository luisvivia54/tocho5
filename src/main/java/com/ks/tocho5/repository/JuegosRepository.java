package com.ks.tocho5.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ks.tocho5.model.GameModel;

public interface JuegosRepository extends JpaRepository<GameModel, Integer>{
	
}
