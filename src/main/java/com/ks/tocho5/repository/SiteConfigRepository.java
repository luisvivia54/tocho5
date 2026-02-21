package com.ks.tocho5.repository;

import com.ks.tocho5.model.SiteConfigModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SiteConfigRepository extends JpaRepository<SiteConfigModel, Long> {
    Optional<SiteConfigModel> findByKey(String key);
}