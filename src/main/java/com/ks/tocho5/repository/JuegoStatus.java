package com.ks.tocho5.repository;

import com.ks.tocho5.model.GameStatusModel;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.LockModeType;

public interface JuegoStatus extends JpaRepository<GameStatusModel, Integer> {

  @Override
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<GameStatusModel> findById(Integer id);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Transactional
  @Query("update GameStatusModel g set g.status = :status where g.game_id = :gameId")
  int updateStatus(@Param("gameId") Integer gameId, @Param("status") String status);

  @Transactional
  default int finalById(Integer gameId) {
    return updateStatus(gameId, "FINAL");
  }

  @Query("select g.home_team_id from GameStatusModel g where g.game_id = :gameId")
  Integer findHomeTeamId(@Param("gameId") Integer gameId);

  @Query("select g.away_team_id from GameStatusModel g where g.game_id = :gameId")
  Integer findAwayTeamId(@Param("gameId") Integer gameId);

  List<GameStatusModel> findAllByStatus(String status);

  default List<GameStatusModel> findAllScheduled() {
    return findAllByStatus("SCHEDULED");
  }

  @Query("""
     select g
       from GameStatusModel g
       join fetch g.homeTeam
       join fetch g.awayTeam
       join fetch g.category c
       join fetch g.season s
      where upper(g.status) = 'SCHEDULED'
      order by g.match_date_utc asc
  """)
  List<GameStatusModel> findAllScheduledWithTeams();

  @Query("""
     select g
       from GameStatusModel g
       join fetch g.homeTeam
       join fetch g.awayTeam
       join fetch g.category c
      where upper(g.status) = upper(:status)
      order by g.match_date_utc desc
  """)
  List<GameStatusModel> findByStatusWithTeams(@Param("status") String status, Pageable pageable);

  default List<GameStatusModel> findFinalWithTeams(String status, Pageable pageable) {
    return findByStatusWithTeams(status, pageable);
  }

  // ← leagueId es Long (coincide con CategoryModel.leagueId) + join fetch season
  @Query("""
     select g
       from GameStatusModel g
       join fetch g.homeTeam
       join fetch g.awayTeam
       join fetch g.category c
       join fetch g.season s
      where upper(g.status) = 'FINAL'
        and (:leagueId is null or c.leagueId = :leagueId)
        and (:code is null or upper(c.code) = upper(:code))
        and (:gender is null or upper(c.gender) = upper(:gender))
        and (:roundLabel is null or upper(g.roundLabel) = upper(:roundLabel))
      order by g.match_date_utc desc
  """)
  List<GameStatusModel> findFinalWithTeamsFiltered(
      @Param("leagueId") Long leagueId,
      @Param("code") String code,
      @Param("gender") String gender,
      @Param("roundLabel") String roundLabel
  );

  @Query("""
     select g
       from GameStatusModel g
       join fetch g.homeTeam
       join fetch g.awayTeam
       join fetch g.category c
      where g.game_id = :gameId
  """)
  GameStatusModel findOneWithTeams(@Param("gameId") Integer gameId);

  @Query("""
     select g
       from GameStatusModel g
      where (g.home_team_id = :teamId or g.away_team_id = :teamId)
      order by g.match_date_utc desc
  """)
  List<GameStatusModel> findLastGamesForTeam(@Param("teamId") Integer teamId, Pageable pageable);

  // ← leagueId es Long (coincide con CategoryModel.leagueId) + join fetch season
  @Query("""
     select g
       from GameStatusModel g
       join fetch g.homeTeam
       join fetch g.awayTeam
       join fetch g.category c
       join fetch g.season s
      where upper(g.status) = 'SCHEDULED'
        and (:leagueId is null or c.leagueId = :leagueId)
        and (:code is null or upper(c.code) = upper(:code))
        and (:gender is null or upper(c.gender) = upper(:gender))
        and (:roundLabel is null or upper(g.roundLabel) = upper(:roundLabel))
      order by g.match_date_utc asc
  """)
  List<GameStatusModel> findScheduledWithTeamsFiltered(
      @Param("leagueId") Long leagueId,
      @Param("code") String code,
      @Param("gender") String gender,
      @Param("roundLabel") String roundLabel
  );
}