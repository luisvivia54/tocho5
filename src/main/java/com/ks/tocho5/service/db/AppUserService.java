package com.ks.tocho5.service.db;

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

    @Transactional
    public AppUser getOrCreateFromJwt(Jwt jwt) {
        String keycloakId = jwt.getSubject();            // "sub"
        String email = jwt.getClaim("email");
        String name = extractName(jwt);

        return appUserRepository.findByKeycloakId(keycloakId)
                .map(existing -> updateIfNeeded(existing, email, name))
                .orElseGet(() -> createNewUser(keycloakId, email, name));
    }

    private AppUser createNewUser(String keycloakId, String email, String name) {
        AppUser user = new AppUser();
        user.setKeycloakId(keycloakId);
        user.setEmail(email);
        user.setFullName(name);
        user.setActive(true);
        return appUserRepository.save(user);
    }

    private AppUser updateIfNeeded(AppUser user, String email, String name) {
        boolean changed = false;

        if (email != null && !email.equals(user.getEmail())) {
            user.setEmail(email);
            changed = true;
        }

        if (name != null && !name.equals(user.getFullName())) {
            user.setFullName(name);
            changed = true;
        }

        if (changed) {
            user = appUserRepository.save(user);
        }

        return user;
    }

    private String extractName(Jwt jwt) {
        // Dependiendo de tu mapeo de claims en Keycloak:
        String name = jwt.getClaim("name");
        if (name != null) return name;

        String given = jwt.getClaim("given_name");
        String family = jwt.getClaim("family_name");
        if (given != null || family != null) {
            return (given == null ? "" : given) + " " + (family == null ? "" : family);
        }

        String preferred = jwt.getClaim("preferred_username");
        if (preferred != null) return preferred;

        return jwt.getSubject(); // fallback
    }
}
