package com.ks.tocho5.model.dto;

import java.util.UUID;

public record AssetDTO(
        UUID id,
        String publicUrl,
        String r2Key,
        String contentType,
        long sizeBytes
) {}