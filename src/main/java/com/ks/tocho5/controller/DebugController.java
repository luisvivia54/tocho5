package com.ks.tocho5.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de diagnóstico. SOLO admin.
 *
 * - GET /api/_debug/whoami
 *     Devuelve lo que el backend parsea del JWT del request: sub, email, roles
 *     convertidos, claims relevantes. Sirve para debuggear por qué un request
 *     está dando 401/403.
 *
 * - GET /api/_debug/ping
 *     Health público (lo dejamos público en SecurityConfig para probar
 *     conectividad y CORS desde el browser).
 */
@RestController
@RequestMapping("/api/_debug")
public class DebugController {

  @GetMapping("/ping")
  public Map<String, Object> ping() {
    Map<String, Object> r = new LinkedHashMap<>();
    r.put("ok", true);
    r.put("service", "tocho5");
    return r;
  }

  @GetMapping("/whoami")
  @PreAuthorize("hasRole('admin')")
  public Map<String, Object> whoami(
      @AuthenticationPrincipal Jwt jwt,
      Authentication auth
  ) {
    Map<String, Object> r = new LinkedHashMap<>();

    if (jwt == null) {
      r.put("authenticated", false);
      return r;
    }

    r.put("authenticated", true);
    r.put("sub", jwt.getSubject());
    r.put("email", jwt.getClaimAsString("email"));
    r.put("preferred_username", jwt.getClaimAsString("preferred_username"));
    r.put("name", jwt.getClaimAsString("name"));
    r.put("issuer", jwt.getIssuer() != null ? jwt.getIssuer().toString() : null);
    r.put("audience", jwt.getAudience());
    r.put("expiresAt", jwt.getExpiresAt() != null ? jwt.getExpiresAt().toString() : null);

    Object realmAccess = jwt.getClaim("realm_access");
    if (realmAccess instanceof Map<?, ?> ra) {
      r.put("realm_roles", ra.get("roles"));
    }

    List<String> authorities = auth.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .toList();
    r.put("authorities", authorities);

    return r;
  }
}
