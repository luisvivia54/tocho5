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
    //
    // Arranca DESDE player (equipos activos de la liga) y hace LEFT JOIN con las
    // stats de la temporada, para que también aparezcan los jugadores SIN stats
    // (con ceros). La subconsulta 's' acota las stats a la temporada; si un jugador
    // no jugó, 's' es null y los COALESCE lo dejan en 0.
    @Query(value = """
    	    SELECT
    	      md5(lower(trim(p.curp)))            AS personKey,
    	      MAX(p.player_id)                    AS playerId,
    	      p.team_id                           AS teamId,
    	      MAX(p.full_name)                    AS fullName,
    	      MAX(p.photo_url)                    AS photoUrl,
    	      MAX(p.jersey_number)                AS number,
    	      COALESCE(SUM(s.td), 0)              AS td,
    	      COALESCE(SUM(s.pass_td), 0)         AS passTd,
    	      COALESCE(SUM(s.interceptions), 0)   AS intercep,
    	      COALESCE(SUM(s.sacks), 0)           AS sacks
    	    FROM public.player p
    	    JOIN public.team t ON t.team_id = p.team_id
    	    LEFT JOIN (
    	        SELECT spg.player_id, spg.team_id,
    	               spg.td, spg.pass_td, spg.interceptions, spg.sacks
    	        FROM public.stat_player_game spg
    	        JOIN public.game g ON g.game_id = spg.game_id
    	        WHERE g.season_id = :seasonId
    	    ) s ON s.player_id = p.player_id AND s.team_id = p.team_id
    	    WHERE t.league_id = :leagueId
    	      AND t.is_active = true
    	    GROUP BY md5(lower(trim(p.curp))), p.team_id
    	    ORDER BY td DESC, passTd DESC, intercep DESC, sacks DESC, fullName ASC
    	""", nativeQuery = true)
    	List<PlayerSeasonStatsProjection> leaderboardBySeason(
    	        @Param("seasonId") Long seasonId,
    	        @Param("leagueId") Long leagueId);

}
