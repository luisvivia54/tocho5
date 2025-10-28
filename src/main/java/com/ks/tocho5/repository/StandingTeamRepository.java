package com.ks.tocho5.repository;
import com.ks.tocho5.model.StandingTeamModel;

//Si status es enum, importa tu enum y cambia el tipo de parámetro
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface StandingTeamRepository extends JpaRepository<StandingTeamModel,Integer> {

	 // suma 1 (o el incremento que mandes) al campo gp
	  @Modifying(clearAutomatically = true, flushAutomatically = true)
	  @Transactional
	  @Query("update StandingTeamModel s set s.gp = s.gp + :inc where s.team_id = :id")
	  int incrementGp(@Param("id") Integer teamId, @Param("inc") int inc);
	  
	  @Modifying(clearAutomatically = true, flushAutomatically = true)
	  @Transactional
	  @Query("update StandingTeamModel s set s.wins = s.wins + :inc where s.team_id = :id")
	  int incrementWins(@Param("id") Integer teamId, @Param("inc") int inc);
	  
	  @Modifying(clearAutomatically = true, flushAutomatically = true)
	  @Transactional
	  @Query("update StandingTeamModel s set s.losses = s.losses + :inc where s.team_id = :id")
	  int incrementLosses(@Param("id") Integer teamId, @Param("inc") int inc);
	  
	  @Modifying(clearAutomatically = true, flushAutomatically = true)
	  @Transactional
	  @Query("update StandingTeamModel s set s.draws = s.draws + :inc where s.team_id = :id")
	  int incrementDraws(@Param("id") Integer teamId, @Param("inc") int inc);
	  
	  @Modifying(clearAutomatically = true, flushAutomatically = true)
	  @Transactional
	  @Query("update StandingTeamModel s set s.points_for = s.points_for + :inc where s.team_id = :id")
	  int incrementPointsFor(@Param("id") Integer teamId, @Param("inc") int inc);
	  
	  @Modifying(clearAutomatically = true, flushAutomatically = true)
	  @Transactional
	  @Query("update StandingTeamModel s set s.points_against = s.points_against + :inc where s.team_id = :id")
	  int incrementPointsAgainst(@Param("id") Integer teamId, @Param("inc") int inc);
	  
	  @Modifying(clearAutomatically = true, flushAutomatically = true)
	  @Transactional
	  @Query("update StandingTeamModel s set s.table_points = s.table_points + :inc where s.team_id = :id")
	  int incrementTablePoints(@Param("id") Integer teamId, @Param("inc") int inc);
	  
	  // helper para sumar 1
	  @Transactional
	  default int addOneToGp(Integer teamId) {
	    return incrementGp(teamId, 1);
	  }
	  
	  @Transactional
	  default int addOneToWins(Integer teamId) {
	    return incrementWins(teamId, 1);
	  }
	  
	  @Transactional
	  default int addOneToLosses(Integer teamId) {
	    return incrementLosses(teamId, 1);
	  }
	  
	  @Transactional
	  default int addOneToDraws(Integer teamId) {
	    return incrementDraws(teamId, 1);
	  }
	  
	  @Transactional
	  default int addPointsFor(Integer teamId, Integer points) {
	    return incrementPointsFor(teamId, points);
	  }
	  
	  @Transactional
	  default int addPointsAgainst(Integer teamId, Integer points) {
	    return incrementPointsAgainst(teamId, points);
	  }
	  
	  @Transactional
	  default int addTablePoints(Integer teamId, Integer points) {
	    return incrementTablePoints(teamId, points);
	  }
}
