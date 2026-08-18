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
    
    // Leaderboard agrupado por PERSONA (hash de la CURP) y equipo.
    // personKey = md5(curp normalizada): permite al front juntar al mismo jugador
    // que aparece en varios equipos, sin exponer la CURP real. Cada fila sigue
    // siendo (persona x equipo) para poder mostrar el desglose por equipo/categoría.
    @Query(value = """
    	    SELECT
    	      md5(lower(trim(p.curp)))        AS personKey,
    	      MAX(p.player_id)                AS playerId,
    	      spg.team_id                     AS teamId,
    	      MAX(p.full_name)                AS fullName,
    	      COALESCE(SUM(spg.td), 0)        AS td,
    	      COALESCE(SUM(spg.pass_td), 0)   AS passTd,
    	      COALESCE(SUM(spg.interceptions), 0)  AS intercep,
    	      COALESCE(SUM(spg.sacks), 0)     AS sacks
    	    FROM public.stat_player_game spg
    	    JOIN public.game g   ON g.game_id = spg.game_id
    	    JOIN public.player p ON p.player_id = spg.player_id
    	    WHERE g.season_id = :seasonId
    	    GROUP BY md5(lower(trim(p.curp))), spg.team_id
			    ORDER BY td DESC, passTd DESC, intercep DESC, sacks DESC
    	""", nativeQuery = true)
    	List<PlayerSeasonStatsProjection> leaderboardBySeason(@Param("seasonId") Long seasonId);

}
