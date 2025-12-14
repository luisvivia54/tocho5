package com.ks.tocho5.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

import com.ks.tocho5.model.TeamEnrollmentInfoProjection;

import com.ks.tocho5.model.EquiposProjection;
import com.ks.tocho5.model.EquiposStatsModel;

public interface EquiposFiltroRepository extends JpaRepository<EquiposStatsModel, Integer>{
	@Query(
		    value = """
		      SELECT 
		        e.team_id      AS team_id,
		        t.name         AS name,
		        e.category_id  AS category_id,
		        e.season_id    AS season_id
		      FROM team_enrollment e
		      JOIN team t ON t.team_id = e.team_id
		      WHERE
		        (:name IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%')))
		        AND (:category_id IS NULL OR e.category_id = :category_id)
		        AND (:season_id  IS NULL OR e.season_id  = :season_id)
		        AND (:team_id    IS NULL OR e.team_id    = :team_id) 
		      ORDER BY t.name ASC
		      """,
		    countQuery = """
		      SELECT COUNT(*)
		      FROM team_enrollment e
		      JOIN team t ON t.team_id = e.team_id
		      WHERE
		        (:name IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%')))
		        AND (:categoryId IS NULL OR e.category_id = :categoryId)
		        AND (:seasonId  IS NULL OR e.season_id  = :seasonId)
		      """,
		    nativeQuery = true
		  )
		  Page<EquiposProjection> searchTeams(
		      @Param("name") String name,
		      @Param("category_id") Integer category_id,
		      @Param("season_id") Integer season_id,
		      @Param("team_id") Integer team_id,
		      Pageable pageable
		  );

	@Query(value = """
		    SELECT 
		      e.team_id        AS teamId,
		      e.season_id      AS seasonId,
		      s.name           AS seasonName,
		      e.category_id    AS categoryId,
		      c.name           AS categoryName,
		      c.gender         AS categoryGender,
		      c.code           AS categoryCode
		    FROM team_enrollment e
		    JOIN season s   ON s.season_id   = e.season_id
		    JOIN category c ON c.category_id = e.category_id
		    WHERE e.team_id = :teamId
		    ORDER BY e.season_id DESC
		    LIMIT 1
		    """,
		    nativeQuery = true
		)
		Optional<TeamEnrollmentInfoProjection> findLatestEnrollmentForTeam(
		    @Param("teamId") Integer teamId
		);

}
