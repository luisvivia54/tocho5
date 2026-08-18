package com.ks.tocho5.model;

public record PlayerSeasonStatsDTO(
        String personKey,
        Long playerId,
        Long teamId,
        String fullName,
        String photoUrl,
        Integer number,
        Integer td,
        Integer passTd,
        Integer intercep,
        Integer sacks
) {}
