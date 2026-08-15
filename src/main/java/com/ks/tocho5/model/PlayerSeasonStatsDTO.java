package com.ks.tocho5.model;

public record PlayerSeasonStatsDTO(
        String personKey,
        Long playerId,
        Long teamId,
        String fullName,
        Integer td,
        Integer passTd,
        Integer intercep,
        Integer sacks
) {}
