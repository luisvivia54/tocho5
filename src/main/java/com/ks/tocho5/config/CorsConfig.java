// src/main/java/com/ks/tocho5/config/CorsConfig.java
package com.ks.tocho5.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();

        // ✅ Dev + Prod
        c.setAllowedOriginPatterns(List.of(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "https://tochero5.mx",
            "https://www.tochero5.mx"
            // agrega aquí otros frontends si tienes:
            // "https://admin.tochero5.mx"
        ));

        // Si usas solo Bearer token, igual funciona en true.
        // (No uses "*" con credentials true)
        c.setAllowCredentials(true);

        c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        c.setAllowedHeaders(List.of(
            "Authorization",
            "Content-Type",
            "Accept",
            "Origin",
            "X-Requested-With"
        ));

        c.setExposedHeaders(List.of("Authorization", "Location"));
        c.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", c);
        return source;
    }
}
