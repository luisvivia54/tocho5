package com.ks.tocho5.repository;

import com.ks.tocho5.model.AppUser;
import com.ks.tocho5.model.EquiposModel;
import com.ks.tocho5.model.TeamListProjection;
import com.ks.tocho5.model.TeamSearchProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EquiposRepository extends JpaRepository<EquiposModel, Long> {

    // ✅ Lista de equipos + ÚLTIMA inscripción (season/category) + datos de category
    @Query(value = """
        SELECT
          t.team_id        AS teamId,
          t.league_id      AS leagueId,

          t.name           AS name,
          t.short_name     AS shortName,
          t.color_primary  AS colorPrimary,
          t.color_secondary AS colorSecondary,
          t.logo_url       AS logoUrl,
          t.is_active      AS isActive,

          en.season_id     AS seasonId,
          en.category_id   AS categoryId,
          c.name           AS categoryName,
          c.code           AS categoryCode,
          c.gender         AS categoryGender

        FROM team t

        -- 👇 agarra la inscripción más reciente por team (por season_id desc)
        LEFT JOIN LATERAL (
          SELECT e.season_id, e.category_id
          FROM team_enrollment e
          WHERE e.team_id = t.team_id
          ORDER BY e.season_id DESC
          LIMIT 1
        ) en ON TRUE

        LEFT JOIN category c ON c.category_id = en.category_id

        WHERE (:leagueId IS NULL OR t.league_id = :leagueId)
          AND (:gender IS NULL OR UPPER(c.gender) = :gender)
          AND (:code   IS NULL OR UPPER(c.code)   = :code)
          AND t.is_active = true

        ORDER BY t.name ASC
        """, nativeQuery = true)
    List<TeamListProjection> findTeamsList(
            @Param("leagueId") Integer leagueId,
            @Param("code") String code,
            @Param("gender") String gender
    );

    int countByCaptain(AppUser captain);
    List<EquiposModel> findByCaptain(AppUser captain);
    
    List<EquiposModel> findByIsActiveTrueOrderByNameAsc();

    @Query(value = """
        SELECT t.*
        FROM team t
        LEFT JOIN LATERAL (
          SELECT e.season_id, e.category_id
          FROM team_enrollment e
          WHERE e.team_id = t.team_id
          ORDER BY e.season_id DESC
          LIMIT 1
        ) en ON TRUE
        LEFT JOIN category c ON c.category_id = en.category_id
        WHERE t.is_active = true
          AND (:leagueId IS NULL OR t.league_id = :leagueId)
          AND (:gender IS NULL OR UPPER(c.gender) = :gender)
          AND (:code   IS NULL OR UPPER(c.code)   = :code)
        ORDER BY t.name ASC
    """, nativeQuery = true)
    List<EquiposModel> findActiveTeamsFiltered(
            @Param("leagueId") Integer leagueId,
            @Param("code") String code,
            @Param("gender") String gender
    );

    @Query(value = """
        WITH search_input AS (
          SELECT LOWER(TRANSLATE(TRIM(CAST(:query AS TEXT)),
            'ÁÀÂÄÃÅáàâäãåÉÈÊËéèêëÍÌÎÏíìîïÓÒÔÖÕóòôöõÚÙÛÜúùûüÑñÇç',
            'AAAAAAaaaaaaEEEEeeeeIIIIiiiiOOOOOoooooUUUUuuuuNnCc'
          )) AS q_norm
        ),
        team_candidates AS (
          SELECT
            t.team_id          AS teamId,
            t.league_id        AS leagueId,
            t.name             AS name,
            t.short_name       AS shortName,
            t.color_primary    AS colorPrimary,
            t.color_secondary  AS colorSecondary,
            t.logo_url         AS logoUrl,
            t.is_active        AS isActive,
            en.season_id       AS seasonId,
            en.category_id     AS categoryId,
            c.name             AS categoryName,
            c.code             AS categoryCode,
            c.gender           AS categoryGender,
            LOWER(TRANSLATE(COALESCE(t.name, ''),
              'ÁÀÂÄÃÅáàâäãåÉÈÊËéèêëÍÌÎÏíìîïÓÒÔÖÕóòôöõÚÙÛÜúùûüÑñÇç',
              'AAAAAAaaaaaaEEEEeeeeIIIIiiiiOOOOOoooooUUUUuuuuNnCc'
            ))                 AS nameNorm,
            LOWER(TRANSLATE(COALESCE(t.short_name, ''),
              'ÁÀÂÄÃÅáàâäãåÉÈÊËéèêëÍÌÎÏíìîïÓÒÔÖÕóòôöõÚÙÛÜúùûüÑñÇç',
              'AAAAAAaaaaaaEEEEeeeeIIIIiiiiOOOOOoooooUUUUuuuuNnCc'
            ))                 AS shortNameNorm
          FROM team t
          LEFT JOIN LATERAL (
            SELECT e.season_id, e.category_id
            FROM team_enrollment e
            WHERE e.team_id = t.team_id
            ORDER BY e.season_id DESC
            LIMIT 1
          ) en ON TRUE
          LEFT JOIN category c ON c.category_id = en.category_id
          WHERE t.is_active = true
            AND (:leagueId IS NULL OR t.league_id = :leagueId)
            AND (:gender IS NULL OR UPPER(c.gender) = :gender)
            AND (:code   IS NULL OR UPPER(c.code)   = :code)
        )
        SELECT
          tc.teamId           AS teamId,
          tc.leagueId         AS leagueId,
          tc.name             AS name,
          tc.shortName        AS shortName,
          tc.colorPrimary     AS colorPrimary,
          tc.colorSecondary   AS colorSecondary,
          tc.logoUrl          AS logoUrl,
          tc.isActive         AS isActive,
          tc.seasonId         AS seasonId,
          tc.categoryId       AS categoryId,
          tc.categoryName     AS categoryName,
          tc.categoryCode     AS categoryCode,
          tc.categoryGender   AS categoryGender,
          CASE
            WHEN tc.nameNorm = s.q_norm THEN 0
            WHEN tc.shortNameNorm = s.q_norm THEN 1
            WHEN tc.nameNorm LIKE s.q_norm || '%%' THEN 2
            WHEN tc.shortNameNorm LIKE s.q_norm || '%%' THEN 3
            WHEN tc.nameNorm LIKE '%%' || s.q_norm || '%%' THEN 4
            ELSE 5
          END                 AS matchRank,
          CASE
            WHEN tc.nameNorm = s.q_norm THEN 'exact_name'
            WHEN tc.shortNameNorm = s.q_norm THEN 'exact_short_name'
            WHEN tc.nameNorm LIKE s.q_norm || '%%' THEN 'prefix_name'
            WHEN tc.shortNameNorm LIKE s.q_norm || '%%' THEN 'prefix_short_name'
            WHEN tc.nameNorm LIKE '%%' || s.q_norm || '%%' THEN 'partial_name'
            ELSE 'partial_short_name'
          END                 AS matchType
        FROM team_candidates tc
        CROSS JOIN search_input s
        WHERE tc.nameNorm LIKE '%%' || s.q_norm || '%%'
           OR tc.shortNameNorm LIKE '%%' || s.q_norm || '%%'
        ORDER BY
          matchRank ASC,
          CASE
            WHEN POSITION(s.q_norm IN tc.nameNorm) > 0 THEN POSITION(s.q_norm IN tc.nameNorm)
            WHEN POSITION(s.q_norm IN tc.shortNameNorm) > 0 THEN POSITION(s.q_norm IN tc.shortNameNorm)
            ELSE 9999
          END ASC,
          LENGTH(tc.name) ASC,
          tc.name ASC
        LIMIT :limit
    """, nativeQuery = true)
    List<TeamSearchProjection> searchActiveTeamsByName(
            @Param("query") String query,
            @Param("leagueId") Integer leagueId,
            @Param("code") String code,
            @Param("gender") String gender,
            @Param("limit") int limit
    );
    
    int countByCaptainAndIsActiveTrue(AppUser captain);
    List<EquiposModel> findByCaptainAndIsActiveTrue(AppUser captain);

}
