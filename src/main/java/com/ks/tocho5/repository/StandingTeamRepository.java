// src/main/java/com/ks/tocho5/repository/StandingTeamRepository.java
package com.ks.tocho5.repository;

import com.ks.tocho5.model.StandingTeamModel;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface StandingTeamRepository extends JpaRepository<StandingTeamModel,Integer> {

  // -------- tus updates (los de gp, wins, etc.) se quedan igual --------
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Transactional
  @Query("update StandingTeamModel s set s.gp = coalesce(s.gp,0) + :inc where s.team_id = :id")
  int incrementGp(@Param("id") Integer teamId, @Param("inc") int inc);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Transactional
  @Query("update StandingTeamModel s set s.wins = coalesce(s.wins,0) + :inc where s.team_id = :id")
  int incrementWins(@Param("id") Integer teamId, @Param("inc") int inc);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Transactional
  @Query("update StandingTeamModel s set s.losses = coalesce(s.losses,0) + :inc where s.team_id = :id")
  int incrementLosses(@Param("id") Integer teamId, @Param("inc") int inc);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Transactional
  @Query("update StandingTeamModel s set s.draws = coalesce(s.draws,0) + :inc where s.team_id = :id")
  int incrementDraws(@Param("id") Integer teamId, @Param("inc") int inc);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Transactional
  @Query("update StandingTeamModel s set s.points_for = coalesce(s.points_for,0) + :inc where s.team_id = :id")
  int incrementPointsFor(@Param("id") Integer teamId, @Param("inc") int inc);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Transactional
  @Query("update StandingTeamModel s set s.points_against = coalesce(s.points_against,0) + :inc where s.team_id = :id")
  int incrementPointsAgainst(@Param("id") Integer teamId, @Param("inc") int inc);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Transactional
  @Query("update StandingTeamModel s set s.table_points = coalesce(s.table_points,0) + :inc where s.team_id = :id")
  int incrementTablePoints(@Param("id") Integer teamId, @Param("inc") int inc);

  // Helpers
  @Transactional default int addOneToGp(Integer teamId)       { return incrementGp(teamId, 1); }
  @Transactional default int addOneToWins(Integer teamId)     { return incrementWins(teamId, 1); }
  @Transactional default int addOneToLosses(Integer teamId)   { return incrementLosses(teamId, 1); }
  @Transactional default int addOneToDraws(Integer teamId)    { return incrementDraws(teamId, 1); }
  @Transactional default int addPointsFor(Integer teamId, Integer p){ return incrementPointsFor(teamId, p); }
  @Transactional default int addPointsAgainst(Integer teamId, Integer p){ return incrementPointsAgainst(teamId, p); }
  @Transactional default int addTablePoints(Integer teamId, Integer p){ return incrementTablePoints(teamId, p); }

  // -------- NUEVOS: traer standings + nombre del team en una sola consulta --------

  // Todos
  @Query("""
         select s
           from StandingTeamModel s
           join fetch s.team
         """)
  List<StandingTeamModel> findAllWithTeam();

  // Filtrado típico (ej. por temporada/categoría)
  @Query("""
         select s
           from StandingTeamModel s
           join fetch s.team
          where s.season_id = :seasonId
            and s.category_id = :categoryId
         """)
  List<StandingTeamModel> findBySeasonAndCategoryWithTeam(@Param("seasonId") Integer seasonId,
                                                          @Param("categoryId") Integer categoryId);
}

