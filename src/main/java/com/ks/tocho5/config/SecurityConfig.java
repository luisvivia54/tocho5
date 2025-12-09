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

            // 🔓 TODO el mundo puede acceder a TODO
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );

            // 👇 IMPORTANTE: por ahora SIN resource server
            // .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}
