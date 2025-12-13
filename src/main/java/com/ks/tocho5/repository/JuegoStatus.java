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

  // === Queries simples por IDs ===
  @Query("select g.home_team_id from GameStatusModel g where g.game_id = :id")
  Integer findHomeTeamId(@Param("id") Integer gameId);

  @Query("select g.away_team_id from GameStatusModel g where g.game_id = :id")
  Integer findAwayTeamId(@Param("id") Integer gameId);

  List<GameStatusModel> findAllByStatus(String status);

  default List<GameStatusModel> findAllScheduled() {
    return findAllByStatus("SCHEDULED");
  }

  // === Partidos SCHEDULED con equipos cargados (para nombres) ===
  @Query("""
         select g
           from GameStatusModel g
           join fetch g.homeTeam
           join fetch g.awayTeam
          where g.status = 'SCHEDULED'
          order by g.match_date_utc asc
         """)
  List<GameStatusModel> findAllScheduledWithTeams();

  // Partidos FINALIZADOS (o el status que mandes) con equipos
  @Query("""
         select g
           from GameStatusModel g
           join fetch g.homeTeam
           join fetch g.awayTeam
          where g.status = :status
          order by g.updated_at desc, g.game_id desc
         """)
  List<GameStatusModel> findFinalWithTeams(@Param("status") String status, Pageable pageable);

  // Un solo partido con equipos
  @Query("""
         select g
           from GameStatusModel g
           join fetch g.homeTeam
           join fetch g.awayTeam
          where g.game_id = :id
         """)
  GameStatusModel findOneWithTeams(@Param("id") Integer gameId);

  // Últimos juegos de un equipo
  @Query("""
         select g
           from GameStatusModel g
          where (g.home_team_id = :teamId or g.away_team_id = :teamId)
          order by g.match_date_utc desc
         """)
  List<GameStatusModel> findLastGamesForTeam(
          @Param("teamId") Integer teamId,
          Pageable pageable
  );

  // === VERSIÓN SAFE DEL FILTRADO POR league/category/gender ===
  // Por ahora IGNORA los filtros y solo reusa la consulta buena,
  // para que el backend levante sin tronar por 'league_id'.
  default List<GameStatusModel> findScheduledWithTeamsFiltered(
          Integer leagueId,
          String categoryCode,
          String gender
  ) {
    return findAllScheduledWithTeams();
  }
}
