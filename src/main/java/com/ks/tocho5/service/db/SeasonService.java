package com.ks.tocho5.service.db;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeasonService {

    private final JdbcTemplate jdbc;

    public SeasonService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public Long getCurrentSeasonId(Long leagueId) {
        Long seasonId = jdbc.queryForObject("""
            SELECT season_id
            FROM public.season
            WHERE league_id = ?
              AND is_active = true
            LIMIT 1
        """, Long.class, leagueId);

        if (seasonId == null) throw new RuntimeException("No hay temporada activa para leagueId=" + leagueId);
        return seasonId;
    }
}
