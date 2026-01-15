package com.ks.tocho5.service.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class KeycloakAdminService {

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
     * (Admin no lo tocamos aquí)
     */
    public void setExclusiveRealmRoleUserOrCaptain(String keycloakUserId, String desiredRole) {
        String role = desiredRole == null ? "" : desiredRole.toLowerCase(Locale.ROOT).trim();
        if (!role.equals("user") && !role.equals("captain")) {
            throw new IllegalArgumentException("desiredRole must be 'user' or 'captain'");
        }

        UserResource user = realm().users().get(keycloakUserId);
        RoleScopeResource realmLevel = user.roles().realmLevel();

        RoleRepresentation rrUser = realm().roles().get("user").toRepresentation();
        RoleRepresentation rrCaptain = realm().roles().get("captain").toRepresentation();

        // remover ambos primero
        List<RoleRepresentation> toRemove = new ArrayList<>();
        toRemove.add(rrUser);
        toRemove.add(rrCaptain);
        realmLevel.remove(toRemove); // remove(List<RoleRepresentation>) :contentReference[oaicite:2]{index=2}

        // agregar el deseado
        realmLevel.add(List.of(role.equals("captain") ? rrCaptain : rrUser)); // add(List<RoleRepresentation>) :contentReference[oaicite:3]{index=3}
    }

    /** Fuerza logout de todas las sesiones del usuario => relogin requerido */
    public void forceLogout(String keycloakUserId) {
        realm().users().get(keycloakUserId).logout(); // logout() :contentReference[oaicite:4]{index=4}
    }
}
