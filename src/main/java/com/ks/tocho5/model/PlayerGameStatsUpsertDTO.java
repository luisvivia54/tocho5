package com.ks.tocho5.model;

public record PlayerGameStatsUpsertDTO(
        Long playerId,
        Long teamId,
        Integer td,
        Integer passTd,
        Integer intercep,
        Integer sacks
) {}
