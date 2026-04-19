// src/main/java/com/ks/tocho5/config/SecurityConfig.java
package com.ks.tocho5.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Seguridad de Tocho5:
 *  - API stateless basada en JWT de Keycloak (OAuth2 Resource Server).
 *  - CSRF deshabilitado porque NO hay sesiones/cookies (API con Bearer).
 *  - CORS manejado por CorsConfigurationSource (ver CorsConfig).
 *  - Cualquier endpoint no listado como público exige JWT válido.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      CorsConfigurationSource corsConfigurationSource
  ) throws Exception {

    http
      // CORS integrado con Spring Security (no depende de WebMvcConfigurer)
      .cors(cors -> cors.configurationSource(corsConfigurationSource))

      // Sin CSRF porque no hay sesiones con cookies (API Bearer)
      .csrf(AbstractHttpConfigurer::disable)

      // API stateless: cada request trae su JWT
      .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

      // Sin login form, sin basic
      .httpBasic(AbstractHttpConfigurer::disable)
      .formLogin(AbstractHttpConfigurer::disable)
      .logout(AbstractHttpConfigurer::disable)

      // Cabeceras seguras por defecto
      .headers(h -> h
          .contentTypeOptions(c -> {})
          .frameOptions(f -> f.deny())
          .referrerPolicy(r -> r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
      )

      .authorizeHttpRequests(auth -> auth

        // ========= Preflight CORS =========
        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

        // ========= Actuator / Health (si se usa) =========
        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

        // ========= ADMIN (solo rol admin) =========
        .requestMatchers("/api/admin/**").hasRole("admin")

        // ========= Públicos (GET) =========
        .requestMatchers(HttpMethod.GET,
            "/api/teams",
            "/api/teams/list",
            "/api/teams/search",
            "/api/games",
            "/api/gamesFinal",
            "/api/points",
            "/api/categories",
            "/api/stats/players",
            "/api/seasons",
            "/api/seasons/list",
            "/api/seasons/current",
            "/api/site-configs/home"
        ).permitAll()

        // Detalle público de equipos (path variable)
        .requestMatchers(HttpMethod.GET,
            "/api/teams/*/detail",
            "/api/teams/*/players",
            "/api/teams/*/photos"
        ).permitAll()

        // ========= Escrituras peligrosas: SOLO admin =========
        // Update de marcadores en batch (antes estaba abierto)
        .requestMatchers(HttpMethod.POST, "/api/partido/update").hasRole("admin")
        .requestMatchers(HttpMethod.POST, "/api/games").hasRole("admin")
        .requestMatchers(HttpMethod.DELETE, "/api/games/**").hasRole("admin")
        .requestMatchers(HttpMethod.PUT,  "/api/games/*/player-stats").hasAnyRole("admin", "captain")
        .requestMatchers(HttpMethod.PUT,  "/api/site-configs/**").hasRole("admin")

        // ========= Todo lo demás requiere JWT válido =========
        .anyRequest().authenticated()
      )

      .oauth2ResourceServer(oauth2 -> oauth2
        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
      );

    return http.build();
  }

  /**
   * Keycloak realm_access.roles -> ROLE_admin / ROLE_captain / ROLE_user
   * (todo en minúsculas para que hasRole("admin") lo encuentre)
   */
  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter conv = new JwtAuthenticationConverter();

    conv.setJwtGrantedAuthoritiesConverter(jwt -> {
      Collection<GrantedAuthority> authorities = new ArrayList<>();

      Object realmAccessObj = jwt.getClaim("realm_access");
      if (realmAccessObj instanceof Map<?, ?> realmAccess) {
        Object rolesObj = realmAccess.get("roles");
        if (rolesObj instanceof Collection<?> roles) {
          for (Object r : roles) {
            if (r == null) continue;
            String role = r.toString().toLowerCase(Locale.ROOT).trim();
            if (role.isEmpty()) continue;
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
          }
        }
      }
      return authorities;
    });

    return conv;
  }
}
