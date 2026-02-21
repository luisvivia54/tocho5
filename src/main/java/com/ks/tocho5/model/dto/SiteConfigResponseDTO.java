package com.ks.tocho5.model.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;

public record SiteConfigResponseDTO(
        String key,
        int schemaVersion,
        JsonNode data,
        Instant updatedAt
) {}