package com.ks.tocho5.service.db;

import com.ks.tocho5.model.SeasonLiteDto;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SeasonService {

    private final JdbcTemplate jdbc;

    public SeasonService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public Long getCurrentSeasonId(Long leagueId) {
        try {
            Long seasonId = jdbc.queryForObject("""
                SELECT season_id
                FROM public.season
                WHERE league_id = ?
                  AND is_active = true
                LIMIT 1
            """, Long.class, leagueId);

            if (seasonId == null) throw new RuntimeException("No hay temporada activa para leagueId=" + leagueId);
            return seasonId;

        } catch (EmptyResultDataAccessException ex) {
            throw new RuntimeException("No hay temporada activa para leagueId=" + leagueId);
        }
    }

    /**
     * Lista ligera para dropdowns.
     * GET /api/seasons?leagueId=1
     */
    @Transactional(readOnly = true)
    public List<SeasonLiteDto> listLite(Long leagueId) {

        // 1) Detectar columna de nombre (si existe) para no romper SQL
        Set<String> cols = new HashSet<>(jdbc.queryForList("""
            SELECT lower(column_name)
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'season'
        """, String.class));

        String nameCol = null;
        for (String candidate : List.of("name", "season_name", "title", "label")) {
            if (cols.contains(candidate)) {
                nameCol = candidate;
                break;
            }
        }

        // 2) Armar SELECT name sin referenciar columnas que no existan
        String nameExpr;
        if (nameCol != null) {
            nameExpr = "COALESCE(NULLIF(BTRIM(s." + nameCol + "), ''), 'Temporada ' || s.season_id::text)";
        } else {
            nameExpr = "'Temporada ' || s.season_id::text";
        }

        // ✅ FIX: ya NO usamos "(? IS NULL OR ...)" porque Postgres no infiere el tipo cuando llega NULL
        // Usamos COALESCE con CAST explícito
        String sql = """
            SELECT
              s.season_id AS id,
              %s AS name
            FROM public.season s
            WHERE s.league_id = COALESCE(?::bigint, s.league_id)
            ORDER BY s.is_active DESC NULLS LAST, s.season_id DESC
        """.formatted(nameExpr);

        return jdbc.query(
            sql,
            (rs, i) -> new SeasonLiteDto(
                rs.getLong("id"),
                rs.getString("name")
            ),
            leagueId
        );
    }
}
