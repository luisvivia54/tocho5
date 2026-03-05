// src/main/java/com/ks/tocho5/model/dto/GameCreateRequest.java
package com.ks.tocho5.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GameCreateRequest(

        // si llega id en create, lo bloqueamos
        @JsonAlias({"game_id", "gameId", "id"})
        Integer gameId,

        @JsonAlias({"season_id", "seasonId"})
        Long seasonId,

        @JsonAlias({"category_id", "categoryId"})
        Integer categoryId,

        @JsonAlias({"home_team_id", "homeTeamId"})
        Integer homeTeamId,

        @JsonAlias({"away_team_id", "awayTeamId"})
        Integer awayTeamId,

        // viene del front como 2026-01-24T18:00:00.000Z
        @JsonAlias({"match_date_utc", "matchDateUtc"})
        String matchDateUtc,

        @JsonAlias({"round_label", "roundLabel"})
        String roundLabel,

        // ✅ NUEVO: cancha / sede
        // acepta cualquiera de estos nombres desde el front
        @JsonAlias({"venue", "field", "location", "cancha"})
        String venue

) {}