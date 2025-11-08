// src/main/java/com/ks/tocho5/repository/JuegoStatus.java
package com.ks.tocho5.repository;

import com.ks.tocho5.model.GameStatusModel;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface JuegoStatus extends JpaRepository<GameStatusModel, Integer> {

  // === UPDATE de status (ojo con el valor permitido por el CHECK) ===
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Transactional
  @Query("update GameStatusModel g set g.status = :status where g.game_id = :id")
  int updateStatus(@Param("id") Integer gameId, @Param("status") String status);

  @Transactional
  default int finishById(Integer gameId) {
    // Usa el literal EXACTO permitido por tu CHECK (probablemente 'FINISHED')
    return updateStatus(gameId, "FINISHED");
  }

  // === Queries existentes por IDs (si las quieres conservar) ===
  @Query("select g.home_team_id from GameStatusModel g where g.game_id = :id")
  Integer findHomeTeamId(@Param("id") Integer gameId);
  @Query("select g.away_team_id from GameStatusModel g where g.game_id = :id")
  Integer findAwayTeamId(@Param("id") Integer gameId);

  List<GameStatusModel> findAllByStatus(String status);

  default List<GameStatusModel> findAllScheduled() {
    return findAllByStatus("SCHEDULED");
  }

  // === NUEVO: Traer partidos con equipos cargados (para nombres) ===
  @Query("""
         select g
           from GameStatusModel g
           join fetch g.homeTeam
           join fetch g.awayTeam
          where g.status = 'SCHEDULED'
         """)
  List<GameStatusModel> findAllScheduledWithTeams();
  
  @Query("""
		  select g
		  from GameStatusModel g
		  join fetch g.homeTeam
		  join fetch g.awayTeam
		  where g.status = :status
		  order by g.updated_at desc, g.id desc
		""")
  List<GameStatusModel> findFinalWithTeams(@Param("status") String status, Pageable pageable);


  @Query("""
         select g
           from GameStatusModel g
           join fetch g.homeTeam
           join fetch g.awayTeam
          where g.game_id = :id
         """)
  GameStatusModel findOneWithTeams(@Param("id") Integer gameId);
}
