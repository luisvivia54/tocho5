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
 * - El bean SOLO se crea si hay un client-secret no vacío.
 *   Si falta, la app arranca igual y los endpoints de sincronización de roles
 *   responderán 503 con mensaje claro (ver KeycloakAdminService).
 */
@Configuration
@EnableConfigurationProperties(KeycloakAdminProperties.class)
public class KeycloakAdminConfig {

  private static final Logger log = LoggerFactory.getLogger(KeycloakAdminConfig.class);

  @Bean(name = "keycloakAdminClient", destroyMethod = "close")
  @ConditionalOnProperty(prefix = "keycloak.admin", name = "client-secret")
  public Keycloak keycloakAdminClient(KeycloakAdminProperties p) {
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
