package com.ks.prueba;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
  scanBasePackages = { "com.ks.tocho5", "com.ks.prueba" } // Controllers/Services/etc
)
@EntityScan(basePackages = { "com.ks.tocho5.model" })     // Entidades JPA fuera del paquete raíz
@EnableJpaRepositories(basePackages = { "com.ks.tocho5.repository" }) // Repos fuera del paquete raíz
public class Tocho5Application {
  public static void main(String[] args) {
    SpringApplication.run(Tocho5Application.class, args);
  }
}