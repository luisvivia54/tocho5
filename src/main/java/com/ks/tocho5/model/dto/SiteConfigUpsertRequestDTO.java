package com.ks.tocho5.model.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record SiteConfigUpsertRequestDTO(
        int schemaVersion,
        JsonNode data
) {}