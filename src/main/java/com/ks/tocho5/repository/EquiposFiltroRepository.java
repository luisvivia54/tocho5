package com.ks.tocho5.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.ks.tocho5.model.EquiposProjection;

public interface EquiposFiltroRepository extends JpaRepository<Object, Integer>{
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
		        AND (:categoryId IS NULL OR e.category_id = :categoryId)
		        AND (:seasonId  IS NULL OR e.season_id  = :seasonId)
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
		      @Param("categoryId") Integer category_id,
		      @Param("seasonId") Integer season_id,
		      Pageable pageable
		  );
}
