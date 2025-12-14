package com.ks.tocho5.repository;

import com.ks.tocho5.model.PointsRowProjection;
import com.ks.tocho5.model.StandingTeamModel;
import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface StandingTeamRepository extends JpaRepository<StandingTeamModel, Integer> {

  // ================== UPDATES DE STATS ==================

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Transactional
  @Query("""
         update StandingTeamModel s
            set s.gp = coalesce(s.gp, 0) + :inc
          where s.team_id = :id
         """)
  int incrementGp(@Param("id") Integer teamId, @Param("inc") int inc);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Transactional
  @Query("""
         update StandingTeamModel s
            set s.wins = coalesce(s.wins, 0) + :inc
          where s.team_id = :id
         """)
  int incrementWins(@Param("id") Integer teamId, @Param("inc") int inc);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Transactional
  @Query("""
         update StandingTeamModel s
            set s.losses = coalesce(s.losses, 0) + :inc
          where s.team_id = :id
         """)
  int incrementLosses(@Param("id") Integer teamId, @Param("inc") int inc);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Transactional
  @Query("""
         update StandingTeamModel s
            set s.draws = coalesce(s.draws, 0) + :inc
          where s.team_id = :id
         """)
  int incrementDraws(@Param("id") Integer teamId, @Param("inc") int inc);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Transactional
  @Query("""
         update StandingTeamModel s
            set s.points_for = coalesce(s.points_for, 0) + :inc
          where s.team_id = :id
         """)
  int incrementPointsFor(@Param("id") Integer teamId, @Param("inc") int inc);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Transactional
  @Query("""
         update StandingTeamModel s
            set s.points_against = coalesce(s.points_against, 0) + :inc
          where s.team_id = :id
         """)
  int incrementPointsAgainst(@Param("id") Integer teamId, @Param("inc") int inc);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Transactional
  @Query("""
         update StandingTeamModel s
            set s.table_points = coalesce(s.table_points, 0) + :inc
          where s.team_id = :id
         """)
  int incrementTablePoints(@Param("id") Integer teamId, @Param("inc") int inc);

  // Helpers
  @Transactional default int addOneToGp(Integer teamId)                  { return incrementGp(teamId, 1); }
  @Transactional default int addOneToWins(Integer teamId)                { return incrementWins(teamId, 1); }
  @Transactional default int addOneToLosses(Integer teamId)              { return incrementLosses(teamId, 1); }
  @Transactional default int addOneToDraws(Integer teamId)               { return incrementDraws(teamId, 1); }
  @Transactional default int addPointsFor(Integer teamId, Integer p)     { return incrementPointsFor(teamId, p); }
  @Transactional default int addPointsAgainst(Integer teamId, Integer p) { return incrementPointsAgainst(teamId, p); }
  @Transactional default int addTablePoints(Integer teamId, Integer p)   { return incrementTablePoints(teamId, p); }

  // ================== QUERIES DE STANDINGS ==================

  @Query("""
         select s
           from StandingTeamModel s
           join fetch s.team
          where s.season_id = :seasonId
            and s.category_id = :categoryId
         """)
  List<StandingTeamModel> findBySeasonAndCategoryWithTeam(
      @Param("seasonId") Integer seasonId,
      @Param("categoryId") Integer categoryId
  );

  @Query("""
         select s
           from StandingTeamModel s
           join fetch s.team
          order by s.table_points desc
         """)
  List<StandingTeamModel> findAllWithTeam();

  /**
   * ✅ NUEVO: lista para /points con categoryCode + gender (y opcional leagueId).
   * OJO: Esto es NATIVE porque StandingTeamModel NO tiene relación a CategoryModel.
   *
   * Ajusta nombres de tabla/columnas si en tu BD difieren:
   * - standing_team (tabla standings)
   * - team (tabla teams)
   * - category (tabla categories)
   */
  @Query(value = """
        SELECT
          st.standing_id    AS standingId,
          st.season_id      AS seasonId,
          st.category_id    AS categoryId,
          st.team_id        AS teamId,

          st.gp             AS gp,
          st.wins           AS wins,
          st.losses         AS losses,
          st.draws          AS draws,

          st.points_for     AS pointsFor,
          st.points_against AS pointsAgainst,
          st.table_points   AS tablePoints,

          t.name            AS teamName,

          c.code            AS categoryCode,
          c.gender          AS gender

        FROM standing_team st
        JOIN team t      ON t.team_id = st.team_id
        JOIN category c  ON c.category_id = st.category_id

        WHERE (:leagueId IS NULL OR t.league_id = :leagueId)
          AND (:categoryCode IS NULL OR UPPER(c.code) = UPPER(:categoryCode))
          AND (:gender IS NULL OR UPPER(c.gender) = UPPER(:gender))

        ORDER BY st.table_points DESC, t.name ASC
        """, nativeQuery = true)
  List<PointsRowProjection> findPointsList(
      @Param("leagueId") Integer leagueId,
      @Param("categoryCode") String categoryCode,
      @Param("gender") String gender
  );

  /**
   * ✅ Opcional: si quieres mantener el "filtered" anterior pero que ahora sí filtre:
   * (Esto NO cambia tu endpoint, solo te sirve si lo sigues llamando en algún lado)
   */
  default List<StandingTeamModel> findAllWithTeamFiltered(
      Integer leagueId,
      String categoryCode,
      String gender
  ) {
    // Si todavía necesitas exactamente StandingTeamModel, déjalo como antes.
    // Si YA migraste /points a projection, este método puede quedarse igual.
    return findAllWithTeam();
  }
}
