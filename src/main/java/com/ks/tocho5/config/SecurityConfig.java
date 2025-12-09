// src/main/java/com/ks/tocho5/config/SecurityConfig.java
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

            // 🔓 TODAS LAS RUTAS PERMITIDAS, SIN TOKEN
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );

        // 👇 IMPORTANTE: para esta prueba, NO configuramos oauth2ResourceServer
        // nada de .oauth2ResourceServer(...)

        return http.build();
    }
}
