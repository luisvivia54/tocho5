package com.ks.tocho5.model;

public record PlayerSeasonStatsDTO(
        Long playerId,
        String fullName,
        Integer td,
        Integer passTd,
        Integer intercep,
        Integer sacks
) {}
