package com.ks.tocho5.repository;

import com.ks.tocho5.model.CategoryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryModel, Long> {

	@Query("""
	        select c
	          from CategoryModel c
	         where (:leagueId is null or c.leagueId = :leagueId)
	           and (:gender  is null or c.gender  = :gender)
	         order by c.code asc, c.name asc
	    """)
	    List<CategoryModel> findFiltered(
	            @Param("leagueId") Long leagueId,
	            @Param("gender") String gender
	    );

    // Útil para filtrar standings/equipos/partidos por code+gender
    List<CategoryModel> findByCodeIgnoreCase(String code);

    List<CategoryModel> findByCodeIgnoreCaseAndGenderIgnoreCase(String code, String gender);
}
