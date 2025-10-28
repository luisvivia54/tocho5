package com.ks.tocho5.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ks.tocho5.model.GameStatusModel;
//Si status es enum, importa tu enum y cambia el tipo de parámetro
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface JuegoStatus extends JpaRepository<GameStatusModel, Integer>{

	  @Modifying(clearAutomatically = true, flushAutomatically = true)
	  @Transactional
	  @Query("update GameStatusModel g set g.status = :status where g.gameId = :id")
	  int updateStatus(@Param("id") Integer gameId, @Param("status") String status);

	  @Transactional
	  default int finishById(Integer gameId) {
	    return updateStatus(gameId, "FINISHED");
	  }
}
