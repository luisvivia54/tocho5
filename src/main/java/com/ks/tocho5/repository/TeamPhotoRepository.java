package com.ks.tocho5.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ks.tocho5.model.TeamPhotoModel;

public interface TeamPhotoRepository extends JpaRepository<TeamPhotoModel, Long> {

    List<TeamPhotoModel> findByTeam_TeamIdOrderBySortOrderAsc(Long teamId);

    long countByTeam_TeamId(Long teamId);
}
