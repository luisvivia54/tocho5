// src/main/java/com/ks/tocho5/config/SecurityConfig.java
package com.ks.tocho5.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import org.springframework.http.HttpMethod;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // CORS y CSRF
            .cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)

            // Reglas de autorización
            .authorizeHttpRequests(auth -> auth
                // =======================
                // ENDPOINTS PÚBLICOS (NO PIDEN TOKEN)
                // =======================

                // GET simples
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/teams",      // lista de equipos
                    "/api/games",
                    "/api/gamesFinal",
                    "/api/points"
                ).permitAll()

                // GET con path variable (detalle / jugadores)
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/teams/{teamId}/detail",
                    "/api/teams/{teamId}/players"
                ).permitAll()

                // Opcional: permitir también OPTIONS para CORS (preflight)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // =======================
                // TODO LO DEMÁS: REQUIERE BEARER
                // =======================
          
            )

            // Resource Server JWT (Keycloak)
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}
