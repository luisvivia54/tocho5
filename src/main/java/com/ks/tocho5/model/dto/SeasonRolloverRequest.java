package com.ks.tocho5.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * Body de POST /api/seasons/rollover
 *
 * @param leagueId liga (torneo) sobre la que se hace el rollover
 * @param name     nombre de la nueva temporada (ej. "Apertura 2026")
 */
public record SeasonRolloverRequest(
        @JsonAlias({"league_id", "leagueId"}) Long leagueId,
        String name
) {}
