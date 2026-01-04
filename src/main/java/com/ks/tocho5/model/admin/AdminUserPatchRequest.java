// src/main/java/com/ks/tocho5/model/admin/AdminUserPatchRequest.java
package com.ks.tocho5.model.admin;

public record AdminUserPatchRequest(
        String role,
        Integer maxTeamsAllowed,
        Boolean isActive
) {}
