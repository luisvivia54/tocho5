package com.ks.tocho5.service.db;

import java.util.List;
import java.util.Map;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ks.tocho5.model.AppUser;
import com.ks.tocho5.repository.AppUserRepository;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;

    public AppUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    /**
     * Usa el JWT de Keycloak para:
     * - Leer sub, email, nombre y roles (user/captain/admin)
     * - Crear o actualizar el registro en app_user
     * - Ajustar maxTeamsAllowed si es nuevo
     */
    @Transactional
    public AppUser syncFromJwt(Jwt jwt) {
        String keycloakId = jwt.getSubject();          // "sub"
        String email      = jwt.getClaim("email");
        String name       = extractName(jwt);
        String dbRole     = extractRoleFromJwt(jwt);   // "USER" | "CAPTAIN" | "ADMIN"

        // Buscar al usuario o crearlo en memoria
        AppUser user;
        boolean isNew = false;

        var opt = appUserRepository.findByKeycloakId(keycloakId);
        if (opt.isPresent()) {
            user = opt.get();
        } else {
            user = new AppUser();
            user.setKeycloakId(keycloakId);
            user.setActive(true);
            user.setRole(dbRole);
            user.setMaxTeamsAllowed(initialMaxTeamsForRole(dbRole));
            isNew = true;
        }

        boolean changed = isNew;

        if (email != null && !email.equals(user.getEmail())) {
            user.setEmail(email);
            changed = true;
        }

        if (name != null && !name.equals(user.getFullName())) {
            user.setFullName(name);
            changed = true;
        }

        if (dbRole != null && !dbRole.equals(user.getRole())) {
            user.setRole(dbRole);
            changed = true;
        }

        if (changed) {
            user = appUserRepository.save(user);
        }

        return user;
    }

    /**
     * Determina el rol de negocio a partir de los roles del realm en Keycloak.
     * Realm roles esperados: "user", "captain", "admin".
     */
    private String extractRoleFromJwt(Jwt jwt) {
        String defaultRole = "USER";

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) {
            return defaultRole;
        }

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) realmAccess.get("roles");
        if (roles == null) {
            return defaultRole;
        }

        if (roles.contains("admin")) {
            return "ADMIN";
        } else if (roles.contains("captain")) {
            return "CAPTAIN";
        } else {
            return defaultRole;
        }
    }

    private int initialMaxTeamsForRole(String role) {
        if ("ADMIN".equalsIgnoreCase(role) || "CAPTAIN".equalsIgnoreCase(role)) {
            return 1;
        }
        return 0; // USER normal no puede crear equipos por default
    }

    private String extractName(Jwt jwt) {
        // Dependiendo de tu mapeo de claims en Keycloak:
        String name = jwt.getClaim("name");
        if (name != null) return name;

        String given  = jwt.getClaim("given_name");
        String family = jwt.getClaim("family_name");
        if (given != null || family != null) {
            return (given == null ? "" : given) + " " + (family == null ? "" : family);
        }

        String preferred = jwt.getClaim("preferred_username");
        if (preferred != null) return preferred;

        return jwt.getSubject(); // fallback
    }
}
