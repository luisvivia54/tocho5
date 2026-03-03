// src/main/java/com/ks/tocho5/repository/JuegosRepository.java
package com.ks.tocho5.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ks.tocho5.model.GameModel;

import jakarta.persistence.LockModeType;

public interface JuegosRepository extends JpaRepository<GameModel, Integer> {

  // 🔒 Lock para editar score sin carreras (2 admins a la vez)
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select g from GameModel g where g.game_id = :gameId")
  Optional<GameModel> findByIdForUpdate(@Param("gameId") Integer gameId);
}