// src/main/java/com/ks/tocho5/model/admin/AdminUserRowDTO.java
package com.ks.tocho5.model.admin;

import java.time.OffsetDateTime;

public record AdminUserRowDTO(
        Long id,
        String keycloakId,
        String email,
        String fullName,
        String role,
        Integer maxTeamsAllowed,
        Boolean isActive,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
