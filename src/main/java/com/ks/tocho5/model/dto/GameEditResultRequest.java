package com.ks.tocho5.model.dto;

public record GameEditResultRequest(
        Integer homeScore,
        Integer awayScore,
        String reason // opcional (para auditoría)
) {}