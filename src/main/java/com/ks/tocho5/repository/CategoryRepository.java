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
        SELECT c
        FROM CategoryModel c
        WHERE (:leagueId IS NULL OR c.leagueId = :leagueId)
          AND (:gender IS NULL OR UPPER(c.gender) = UPPER(:gender))
        ORDER BY c.code ASC, c.name ASC
    """)
    List<CategoryModel> findFiltered(
            @Param("leagueId") Long leagueId,
            @Param("gender") String gender
    );

    // Útil para filtrar standings/equipos/partidos por code+gender
    List<CategoryModel> findByCodeIgnoreCase(String code);

    List<CategoryModel> findByCodeIgnoreCaseAndGenderIgnoreCase(String code, String gender);
}
