package com.ks.tocho5.repository;

import com.ks.tocho5.model.StatPlayerGameModel;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ks.tocho5.model.PlayerSeasonStatsProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

import java.util.Optional;

public interface StatPlayerGameRepository extends JpaRepository<StatPlayerGameModel, Long> {
    Optional<StatPlayerGameModel> findByGameIdAndTeamIdAndPlayerId(Long gameId, Long teamId, Long playerId);
    
    @Query(value = """
    	    SELECT
    	      p.player_id AS playerId,
    	      p.full_name AS fullName,
    	      COALESCE(SUM(spg.td), 0)       AS td,
    	      COALESCE(SUM(spg.pass_td), 0)  AS passTd,
    	      COALESCE(SUM(spg.intercep), 0) AS intercep,
    	      COALESCE(SUM(spg.sacks), 0)    AS sacks
    	    FROM public.stat_player_game spg
    	    JOIN public.game g   ON g.game_id = spg.game_id
    	    JOIN public.player p ON p.player_id = spg.player_id
    	    WHERE g.season_id = :seasonId
    	    GROUP BY p.player_id, p.full_name
    	    ORDER BY td DESC, passTd DESC, intercep DESC, sacks DESC
    	""", nativeQuery = true)
    	List<PlayerSeasonStatsProjection> leaderboardBySeason(@Param("seasonId") Long seasonId);

}
