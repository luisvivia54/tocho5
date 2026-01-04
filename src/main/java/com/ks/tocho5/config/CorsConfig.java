// src/main/java/com/ks/tocho5/config/CorsConfig.java
package com.ks.tocho5.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
      // ✅ Dev + Prod (usa patterns para puertos)
      .allowedOriginPatterns(
        "http://localhost:*",
        "http://127.0.0.1:*",
        "https://tochero5.mx",
        "https://www.tochero5.mx"
      )
      .allowedMethods("GET","POST","PUT","PATCH","DELETE","OPTIONS")
      .allowedHeaders("*")
      .exposedHeaders("Authorization","Location")
      .allowCredentials(true)
      .maxAge(3600);
  }
}
