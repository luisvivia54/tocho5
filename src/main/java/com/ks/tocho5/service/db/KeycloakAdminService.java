package com.ks.tocho5.service.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Wrapper sobre Keycloak Admin Client.
 * - Valida inputs antes de llamar a Keycloak (para evitar 404/500 crípticos).
 * - Nunca expone detalles internos al caller: envuelve excepciones.
 */
@Service
public class KeycloakAdminService {

    private static final Logger log = LoggerFactory.getLogger(KeycloakAdminService.class);

    private final Keycloak kc;
    private final String realm;

    public KeycloakAdminService(
            @Qualifier("keycloakAdminClient") Keycloak kc,
            @Value("${keycloak.admin.realm}") String realm) {
        this.kc = kc;
        this.realm = realm;
    }

    private RealmResource realm() {
        return kc.realm(realm);
    }

    /**
     * Deja al usuario con EXACTAMENTE 1 rol de {user, captain}.
     * Admin NO se toca desde aquí.
     */
    public void setExclusiveRealmRoleUserOrCaptain(String keycloakUserId, String desiredRole) {
        if (keycloakUserId == null || keycloakUserId.isBlank()) {
            throw new IllegalArgumentException("keycloakUserId es obligatorio");
        }

        String role = desiredRole == null ? "" : desiredRole.toLowerCase(Locale.ROOT).trim();
        if (!role.equals("user") && !role.equals("captain")) {
            throw new IllegalArgumentException("desiredRole debe ser 'user' o 'captain'");
        }

        try {
            UserResource user = realm().users().get(keycloakUserId);
            RoleScopeResource realmLevel = user.roles().realmLevel();

            RoleRepresentation rrUser = realm().roles().get("user").toRepresentation();
            RoleRepresentation rrCaptain = realm().roles().get("captain").toRepresentation();

            // Remover ambos
            List<RoleRepresentation> toRemove = new ArrayList<>(2);
            toRemove.add(rrUser);
            toRemove.add(rrCaptain);
            realmLevel.remove(toRemove);

            // Asignar el deseado
            realmLevel.add(List.of(role.equals("captain") ? rrCaptain : rrUser));
        } catch (RuntimeException e) {
            log.error("Keycloak setExclusiveRealmRole failed for user {}: {}", keycloakUserId, e.getMessage(), e);
            throw new IllegalStateException("No se pudo actualizar el rol en Keycloak");
        }
    }

    /** Fuerza logout de todas las sesiones del usuario => relogin requerido */
    public void forceLogout(String keycloakUserId) {
        if (keycloakUserId == null || keycloakUserId.isBlank()) {
            // No hay nada que cerrar: log y return para no reventar flujos
            log.warn("forceLogout: keycloakUserId null/blank, skip");
            return;
        }
        try {
            realm().users().get(keycloakUserId).logout();
        } catch (RuntimeException e) {
            log.error("Keycloak forceLogout failed for user {}: {}", keycloakUserId, e.getMessage(), e);
            throw new IllegalStateException("No se pudo cerrar sesión en Keycloak");
        }
    }
}
