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
    private final KeycloakAdminService keycloakAdminService;

    public AdminUserService(
            AppUserRepository appUserRepository,
            KeycloakAdminService keycloakAdminService
    ) {
        this.appUserRepository = appUserRepository;
        this.keycloakAdminService = keycloakAdminService;
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

        boolean roleChanged = false;
        boolean activeChanged = false;
        String desiredKcRole = null; // "user" | "captain" (realm role)

        // =========================
        // Rol (no permitir ADMIN desde aquí)
        // =========================
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

                // solo sincronizamos a Keycloak si el usuario NO es admin y el rol es USER/CAPTAIN
                if (!u.isAdmin()) {
                    String curr = (u.getRole() == null ? "" : u.getRole().trim().toUpperCase(Locale.ROOT));
                    String next = nextRole.trim().toUpperCase(Locale.ROOT);

                    if (!curr.equals(next)) {
                        roleChanged = true;

                        // para KeycloakAdminService (realm roles "user"/"captain")
                        if ("CAPTAIN".equals(next)) desiredKcRole = "captain";
                        else if ("USER".equals(next)) desiredKcRole = "user";
                    }
                }

                u.setRole(nextRole);
            }
        }

        // =========================
        // maxTeamsAllowed
        // =========================
        if (req.maxTeamsAllowed() != null) {
            u.setMaxTeamsAllowed(clampTeams(req.maxTeamsAllowed()));
        }

        // =========================
        // isActive
        // =========================
        if (req.isActive() != null) {
            Boolean curr = u.getActive();
            Boolean next = req.isActive();
            if (curr == null || !curr.equals(next)) activeChanged = true;
            u.setActive(next);
        }

        // Guardar DB primero (si truena, no tocamos KC)
        AppUser saved = appUserRepository.saveAndFlush(u);

        // =========================
        // Sync Keycloak + force relogin
        // =========================
        // Si cambió rol (USER/CAPTAIN), actualiza realm role y forzar logout
        if (roleChanged && desiredKcRole != null) {
            try {
                keycloakAdminService.setExclusiveRealmRoleUserOrCaptain(saved.getKeycloakId(), desiredKcRole);
                keycloakAdminService.forceLogout(saved.getKeycloakId());
            } catch (Exception e) {
                // Si falla KC, puedes decidir:
                // A) reventar y marcar rollback (lo más estricto)
                // B) no reventar y solo loggear (lo más permisivo)
                // Aquí: estricto => para que NO quede inconsistente
                throw new RuntimeException("Falló sincronización con Keycloak: " + e.getMessage(), e);
            }
        }

        // Si lo desactivaste, lo pateas de sus sesiones
        if (activeChanged && Boolean.FALSE.equals(saved.getActive())) {
            try {
                keycloakAdminService.forceLogout(saved.getKeycloakId());
            } catch (Exception e) {
                // no es crítico si ya está desactivado en tu app; tú decides
                // aquí lo dejo estricto también
                throw new RuntimeException("Falló logout en Keycloak: " + e.getMessage(), e);
            }
        }

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
