package com.ks.tocho5.repository;
import com.ks.tocho5.model.GameStatusModel;
//Si status es enum, importa tu enum y cambia el tipo de parámetro
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface JuegoStatus extends JpaRepository<GameStatusModel, Integer>{

	  @Modifying(clearAutomatically = true, flushAutomatically = true)
	  @Transactional
	  @Query("update GameStatusModel g set g.status = :status where g.game_id = :id")
	  int updateStatus(@Param("id") Integer gameId, @Param("status") String status);

	  @Query("select g.home_team_id from GameStatusModel g where g.game_id = :id")
	  Integer findHomeTeamId(@Param("id") Integer gameId); 
	  @Query("select g.away_team_id from GameStatusModel g where g.game_id = :id")
	  Integer findAwayTeamId(@Param("id") Integer gameId);
	  
	  @Transactional
	  default int finishById(Integer gameId) {
	    return updateStatus(gameId, "FINAL");
	  }
}
