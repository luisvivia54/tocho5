package com.ks.tocho5.repository;

import com.ks.tocho5.model.AppUser;
import com.ks.tocho5.model.EquiposModel;
import com.ks.tocho5.model.TeamListProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EquiposRepository extends JpaRepository<EquiposModel, Integer> {

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

        ORDER BY t.name ASC
        """, nativeQuery = true)
    List<TeamListProjection> findTeamsList(
            @Param("leagueId") Integer leagueId,
            @Param("code") String code,
            @Param("gender") String gender
    );

    int countByCaptain(AppUser captain);
    List<EquiposModel> findByCaptain(AppUser captain);
}
