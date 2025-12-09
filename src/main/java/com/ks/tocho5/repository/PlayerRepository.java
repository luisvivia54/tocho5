// src/main/java/com/ks/tocho5/repository/PlayerRepository.java
package com.ks.tocho5.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ks.tocho5.model.PlayerModel;

public interface PlayerRepository extends JpaRepository<PlayerModel, Long> {

    List<PlayerModel> findByTeam_TeamId(Long teamId); // 👈 usa Long
}
