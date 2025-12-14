// src/main/java/com/ks/tocho5/repository/JuegoStatus.java
package com.ks.tocho5.repository;

import com.ks.tocho5.model.GameStatusModel;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface JuegoStatus extends JpaRepository<GameStatusModel, Integer> {

  // === UPDATE de status ===
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Transactional
  @Query("update GameStatusModel g set g.status = :status where g.game_id = :id")
  int updateStatus(@Param("id") Integer gameId, @Param("status") String status);

  @Transactional
  default int finishById(Integer gameId) {
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
		     join fetch g.category c
		     left join fetch g.score s
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
		     join fetch g.category c
		     left join fetch g.score s
		    where g.status = :status
		    order by g.match_date_utc desc
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

  // === OJO: versión SAFE del filtrado ===
  // IMPORTANTE:
  //  - NO tiene @Query
  //  - Es `default`
  //  - NO usa league_id para que no truene el arranque
  @Query("""
		   select g
		     from GameStatusModel g
		     join fetch g.homeTeam
		     join fetch g.awayTeam
		     join fetch g.category c
		     left join fetch g.score s
		    where g.status = 'SCHEDULED'
		      and (:leagueId is null or c.leagueId = :leagueId)
		      and (:categoryCode is null or upper(c.code) = upper(:categoryCode))
		      and (:gender is null or upper(c.gender) = upper(:gender))
		      and (:round is null or g.round_la = :round)
		    order by g.match_date_utc asc
		""")
		List<GameStatusModel> findScheduledWithTeamsFiltered(
		    @Param("leagueId") Integer leagueId,
		    @Param("categoryCode") String categoryCode,
		    @Param("gender") String gender,
		    @Param("round") String round
		);

}
