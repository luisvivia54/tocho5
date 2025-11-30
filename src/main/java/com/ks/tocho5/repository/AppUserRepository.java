package com.ks.tocho5.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ks.tocho5.model.AppUser;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByKeycloakId(String keycloakId);
}
