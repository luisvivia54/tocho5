package com.ks.tocho5.repository;

import com.ks.tocho5.model.SeasonModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeasonRepository extends JpaRepository<SeasonModel, Long> {

    // Si tu Season tiene leagueId/seasonId etc, ajusta el nombre del campo.
    // Si NO tienes liga, borra este método y usa findAll().
	List<SeasonModel> findAllByLeagueIdOrderBySeasonIdDesc(Long leagueId);
}
