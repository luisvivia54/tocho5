// src/main/java/com/ks/tocho5/service/db/AdminUserService.java
package com.ks.tocho5.service.db;

import java.util.Locale;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ks.tocho5.model.AppUser;
import com.ks.tocho5.model.admin.AdminUserPatchRequest;
import com.ks.tocho5.model.admin.AdminUserRowDTO;
import com.ks.tocho5.repository.AppUserRepository;

@Service
public class AdminUserService {

    private final AppUserRepository appUserRepository;

    public AdminUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Transactional(readOnly = true)
    public Page<AdminUserRowDTO> list(String q, String role, Boolean active, int page, int size, String sort) {
        String qNorm = trimToNull(q);
        String roleNorm = normalizeRole(role);

        Sort s = switch (String.valueOf(sort == null ? "" : sort).trim()) {
            case "createdAsc" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "nameAsc"    -> Sort.by(Sort.Direction.ASC, "fullName");
            case "teamsDesc"  -> Sort.by(Sort.Direction.DESC, "maxTeamsAllowed");
            default           -> Sort.by(Sort.Direction.DESC, "createdAt"); // createdDesc
        };

        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(200, size));

        Page<AppUser> p = appUserRepository.adminSearch(
                qNorm,
                roleNorm,
                active,
                PageRequest.of(safePage, safeSize, s)
        );

        return p.map(this::toDto);
    }

    @Transactional
    public AdminUserRowDTO patch(Long id, AdminUserPatchRequest req) {
        if (req == null) throw new RuntimeException("Body vacío");

        AppUser u = appUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));

        // Rol (no permitir ADMIN desde aquí)
        if (req.role() != null) {
            String nextRole = normalizeRole(req.role());
            if (nextRole != null) {
                // si el usuario objetivo YA es ADMIN, no lo cambias
                if (u.isAdmin() && !u.getRole().equalsIgnoreCase(nextRole)) {
                    throw new RuntimeException("No se permite cambiar el rol de un ADMIN.");
                }
                // no permitimos setear ADMIN desde este endpoint
                if ("ADMIN".equalsIgnoreCase(nextRole) && !u.isAdmin()) {
                    throw new RuntimeException("No se permite asignar ADMIN desde esta vista.");
                }
                u.setRole(nextRole);
            }
        }

        // maxTeamsAllowed
        if (req.maxTeamsAllowed() != null) {
            u.setMaxTeamsAllowed(clampTeams(req.maxTeamsAllowed()));
        }

        // isActive
        if (req.isActive() != null) {
            u.setActive(req.isActive());
        }

        AppUser saved = appUserRepository.save(u);
        return toDto(saved);
    }

    private AdminUserRowDTO toDto(AppUser u) {
        return new AdminUserRowDTO(
                u.getId(),
                u.getKeycloakId(),
                u.getEmail(),
                u.getFullName(),
                u.getRole(),
                u.getMaxTeamsAllowed(),
                u.getActive(),
                u.getCreatedAt(),
                u.getUpdatedAt()
        );
    }

    private String trimToNull(String s) {
        if (s == null) return null;
        String v = s.trim();
        return v.isEmpty() ? null : v;
    }

    private String normalizeRole(String v) {
        if (v == null) return null;
        String s = v.trim().toUpperCase(Locale.ROOT);
        if (s.isEmpty()) return null;

        if (s.contains("ADMIN")) return "ADMIN";
        if (s.contains("CAPTAIN") || s.contains("CAPITAN")) return "CAPTAIN";
        if (s.contains("USER") || s.contains("USUARIO")) return "USER";

        return null;
    }

    private int clampTeams(Integer v) {
        int n = (v == null ? 0 : v);
        if (n < 0) return 0;
        if (n > 99) return 99;
        return n;
    }
}
