// src/main/java/com/ks/tocho5/config/SecurityConfig.java
package com.ks.tocho5.config;

import java.util.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    http
      .cors(Customizer.withDefaults())   // ✅ usa el CORS de WebMvcConfigurer
      .csrf(AbstractHttpConfigurer::disable)

      .authorizeHttpRequests(auth -> auth

        // ✅ Preflight
        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

        // ✅ ADMIN
        .requestMatchers("/api/admin/**").hasRole("admin")

        // ✅ Públicos
        .requestMatchers(
          HttpMethod.GET,
          "/api/teams",
          "/api/teams/list",
          "/api/games",
          "/api/gamesFinal",
          "/api/points",
          "/api/categories",
          "/api/stats/players",
          "/api/seasons",
          "/api/seasonsList",
          "/api/seasons/current"
        ).permitAll()

        // ✅ Públicos con path variable
        .requestMatchers(
          HttpMethod.GET,
          "/api/teams/*/detail",
          "/api/teams/*/players",
          "/api/teams/*/photos"
        ).permitAll()

        // ✅ Todo lo demás requiere token
        .anyRequest().authenticated()
      )

      .oauth2ResourceServer(oauth2 -> oauth2
        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
      );

    return http.build();
  }

  /**
   * Keycloak realm_access.roles -> ROLE_admin / ROLE_captain / ROLE_user
   */
  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter conv = new JwtAuthenticationConverter();

    conv.setJwtGrantedAuthoritiesConverter(jwt -> {
      Collection<GrantedAuthority> authorities = new ArrayList<>();

      Map<String, Object> realmAccess = jwt.getClaim("realm_access");
      if (realmAccess != null) {
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
