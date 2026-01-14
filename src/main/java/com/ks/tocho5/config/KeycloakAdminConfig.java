package com.ks.tocho5.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KeycloakAdminProperties.class)
public class KeycloakAdminConfig {

  @Bean(name = "keycloakAdminClient")
  @ConditionalOnProperty(prefix = "keycloak.admin", name = "server-url")
  public Keycloak keycloakAdminClient(KeycloakAdminProperties p) {
    return KeycloakBuilder.builder()
        .serverUrl(p.getServerUrl())
        .realm(p.getRealm())
        .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
        .clientId(p.getClientId())
        .clientSecret(p.getClientSecret())
        .build();
  }
}
