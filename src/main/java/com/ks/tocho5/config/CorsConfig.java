// src/main/java/com/ks/tocho5/config/CorsConfig.java
package com.ks.tocho5.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configura CORS como un bean para que Spring Security lo consuma.
 * Nota: con allowCredentials=true NO podemos usar "*" en allowedHeaders;
 * listamos explícitamente los headers que el front necesita mandar.
 */
@Configuration
public class CorsConfig {

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration cfg = new CorsConfiguration();

    // Orígenes: dev (localhost) + prod
    cfg.setAllowedOriginPatterns(List.of(
        "http://localhost:*",
        "http://127.0.0.1:*",
        "https://tochero5.mx",
        "https://www.tochero5.mx",
        "https://develop.tochero5.mx",
        "https://www.develop.tochero5.mx"
    ));

    cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

    // Headers explícitos (no wildcard porque mandamos credentials)
    cfg.setAllowedHeaders(List.of(
        "Authorization",
        "Content-Type",
        "Accept",
        "Origin",
        "X-Requested-With",
        "X-Trace-Id"
    ));

    cfg.setExposedHeaders(List.of("Authorization", "Location", "X-Trace-Id"));
    cfg.setAllowCredentials(true);
    cfg.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cfg);
    return source;
  }
}
