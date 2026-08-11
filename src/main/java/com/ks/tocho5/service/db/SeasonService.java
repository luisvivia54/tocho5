package com.ks.tocho5.service.db;

import com.ks.tocho5.model.SeasonLiteDto;
import com.ks.tocho5.model.dto.SeasonRolloverResult;
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
     * Inicia una NUEVA temporada para una liga (torneo) y "mueve" a ella a todos
     * los equipos activos de esa liga, dejando intactas las temporadas pasadas.
     *
     * Todo ocurre en una sola transaccion:
     *   1) Toma la temporada activa actual de la liga (origen).
     *   2) Crea la nueva temporada (season_id autoincremental) inactiva.
     *   3) MUEVE (UPDATE) las inscripciones activas de esa liga a la nueva temporada.
     *      Ojo: el trigger AFTER INSERT de team_enrollment NO dispara en UPDATE,
     *      por eso el standing se siembra manualmente en el paso 4.
     *   4) Siembra standing_team en ceros para la nueva temporada (idempotente).
     *   5) Activa la nueva temporada y desactiva la anterior de la misma liga.
     *
     * Solo toca la liga indicada: otros torneos (otras ligas) no se ven afectados.
     *
     * POST /api/seasons/rollover
     */
    @Transactional
    public SeasonRolloverResult startSeasonRollover(Long leagueId, String name) {
        if (leagueId == null) {
            throw new IllegalArgumentException("leagueId es obligatorio");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name es obligatorio");
        }
        final String seasonName = name.trim();

        // 1) Temporada activa actual de la liga (origen). Lanza si no hay ninguna.
        Long oldSeasonId = getCurrentSeasonId(leagueId);

        // 2) Crear la nueva temporada (season_id lo asigna la secuencia), inactiva.
        Long newSeasonId = jdbc.queryForObject("""
            INSERT INTO public.season (league_id, name, is_active)
            VALUES (?, ?, false)
            RETURNING season_id
        """, Long.class, leagueId, seasonName);

        // 3) Mover inscripciones activas de la liga desde la temporada anterior.
        int equiposMovidos = jdbc.update("""
            UPDATE public.team_enrollment e
            SET season_id = ?
            FROM public.team t
            WHERE t.team_id   = e.team_id
              AND t.league_id = ?
              AND t.is_active = true
              AND e.is_active = true
              AND e.season_id = ?
        """, newSeasonId, leagueId, oldSeasonId);

        // 4) Sembrar posiciones en ceros para la nueva temporada.
        //    (streak y last5 se quedan NULL como en el resto de los datos.)
        int standingsCreados = jdbc.update("""
            INSERT INTO public.standing_team
              (season_id, category_id, team_id, gp, wins, losses, draws,
               points_for, points_against, table_points)
            SELECT e.season_id, e.category_id, e.team_id, 0, 0, 0, 0, 0, 0, 0
            FROM public.team_enrollment e
            JOIN public.team t ON t.team_id = e.team_id
            WHERE e.season_id = ?
              AND t.league_id = ?
            ON CONFLICT (season_id, category_id, team_id) DO NOTHING
        """, newSeasonId, leagueId);

        // 5) Switch de activa: apagar las viejas de la liga, prender la nueva.
        jdbc.update("""
            UPDATE public.season
               SET is_active = false
             WHERE league_id = ?
               AND is_active = true
               AND season_id <> ?
        """, leagueId, newSeasonId);

        jdbc.update("""
            UPDATE public.season
               SET is_active = true
             WHERE season_id = ?
        """, newSeasonId);

        return new SeasonRolloverResult(newSeasonId, equiposMovidos, standingsCreados);
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
