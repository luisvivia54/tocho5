package com.ks.tocho5.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Construye el Keycloak Admin Client usando client_credentials.
 *
 * - Solo se crea el bean si está la propiedad keycloak.admin.server-url.
 * - Si falta el client-secret lanzamos un error claro en lugar de fallar
 *   con un 401 críptico en tiempo de uso.
 */
@Configuration
@EnableConfigurationProperties(KeycloakAdminProperties.class)
public class KeycloakAdminConfig {

  private static final Logger log = LoggerFactory.getLogger(KeycloakAdminConfig.class);

  @Bean(name = "keycloakAdminClient", destroyMethod = "close")
  @ConditionalOnProperty(prefix = "keycloak.admin", name = "server-url")
  public Keycloak keycloakAdminClient(KeycloakAdminProperties p) {

    if (p.getClientSecret() == null || p.getClientSecret().isBlank()) {
      // Mensaje claro para no perder horas buscando el error en Keycloak
      throw new IllegalStateException(
          "Falta la ENV var KEYCLOAK_ADMIN_CLIENT_SECRET. " +
          "Configúrala antes de arrancar el servicio."
      );
    }

    log.info("Inicializando Keycloak Admin Client: server={}, realm={}, clientId={}",
        p.getServerUrl(), p.getRealm(), p.getClientId());

    return KeycloakBuilder.builder()
        .serverUrl(p.getServerUrl())
        .realm(p.getRealm())
        .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
        .clientId(p.getClientId())
        .clientSecret(p.getClientSecret())
        .build();
  }
}
