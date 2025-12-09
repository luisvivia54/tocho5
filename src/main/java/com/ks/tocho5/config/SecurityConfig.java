package com.ks.tocho5.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CORS y CSRF
            .cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)

            // Qué endpoints son públicos y cuáles requieren auth
            .authorizeHttpRequests(auth -> auth
            		.requestMatchers(
                            "/api/teams",              // lista de equipos
                            "/api/games",              // partidos
                            "/api/gamesFinal",         // últimos 5
                            "/api/points",             // tabla de posiciones

                            // si ya agregaste estos:
                            "/api/teams/*/detail",     // detalle público de equipo
                            "/api/teams/*/players"     // jugadores de un equipo (solo lectura)
                        ).permitAll()
                .anyRequest().authenticated()
            )

            // Resource Server con JWT (Keycloak)
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}
