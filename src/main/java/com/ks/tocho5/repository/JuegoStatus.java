// src/main/java/com/ks/tocho5/repository/JuegoStatus.java
package com.ks.tocho5.repository;

import com.ks.tocho5.model.GameStatusModel;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface JuegoStatus extends JpaRepository<GameStatusModel, Integer> {

  // =========================
  // UPDATE status
  // =========================
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Transactional
  @Query("update GameStatusModel g set g.status = :status where g.game_id = :gameId")
  int updateStatus(@Param("gameId") Integer gameId, @Param("status") String status);

  @Transactional
  default int finalById(Integer gameId) {
    return updateStatus(gameId, "FINAL");
  }

  // =========================
  // IDs rápidos
  // =========================
  @Query("select g.home_team_id from GameStatusModel g where g.game_id = :gameId")
  Integer findHomeTeamId(@Param("gameId") Integer gameId);

  @Query("select g.away_team_id from GameStatusModel g where g.game_id = :gameId")
  Integer findAwayTeamId(@Param("gameId") Integer gameId);

  // =========================
  // Básicos por status
  // =========================
  List<GameStatusModel> findAllByStatus(String status);

  default List<GameStatusModel> findAllScheduled() {
    return findAllByStatus("SCHEDULED");
  }

  // =========================
  // SCHEDULED con equipos + category
  // =========================
  @Query("""
     select g
       from GameStatusModel g
       join fetch g.homeTeam
       join fetch g.awayTeam
       join fetch g.category c
      where g.status = 'SCHEDULED'
      order by g.match_date_utc asc
  """)
  List<GameStatusModel> findAllScheduledWithTeams();

  // =========================
  // Por status con equipos + category (sirve para FINAL / SCHEDULED / etc)
  // + Pageable para size/limit
  // =========================
  @Query("""
     select g
       from GameStatusModel g
       join fetch g.homeTeam
       join fetch g.awayTeam
       join fetch g.category c
      where g.status = :status
      order by g.match_date_utc desc
  """)
  List<GameStatusModel> findByStatusWithTeams(@Param("status") String status, Pageable pageable);

  // =========================
  // Un partido con equipos + category
  // =========================
  @Query("""
     select g
       from GameStatusModel g
       join fetch g.homeTeam
       join fetch g.awayTeam
       join fetch g.category c
      where g.game_id = :gameId
  """)
  GameStatusModel findOneWithTeams(@Param("gameId") Integer gameId);

  // =========================
  // Últimos juegos de un equipo
  // =========================
  @Query("""
     select g
       from GameStatusModel g
      where (g.home_team_id = :teamId or g.away_team_id = :teamId)
      order by g.match_date_utc desc
  """)
  List<GameStatusModel> findLastGamesForTeam(@Param("teamId") Integer teamId, Pageable pageable);

  // =========================
  // SCHEDULED + filtros: jornada/ gender / code / league
  // OJO: usa round_label (no round_la)
  // =========================
  @Query("""
     select g
       from GameStatusModel g
       join fetch g.homeTeam
       join fetch g.awayTeam
       join fetch g.category c
      where g.status = 'SCHEDULED'
        and (:leagueId is null or c.leagueId = :leagueId)
        and (:code is null or upper(c.code) = upper(:code))
        and (:gender is null or upper(c.gender) = upper(:gender))
        and (:roundLabel is null or g.roundLabel = :roundLabel)
      order by g.match_date_utc asc
  """)
  List<GameStatusModel> findScheduledWithTeamsFiltered(
      @Param("leagueId") Integer leagueId,
      @Param("code") String code,
      @Param("gender") String gender,
      @Param("roundLabel") String roundLabel
  );
}
