package com.ks.tocho5.repository;

import com.ks.tocho5.model.AssetModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AssetRepository extends JpaRepository<AssetModel, UUID> {
    Optional<AssetModel> findByR2Key(String r2Key);
}