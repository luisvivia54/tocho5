package com.ks.tocho5.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Propiedades del Keycloak Admin Client.
 * Los valores vienen de application.properties (todos resueltos desde ENV).
 */
@Validated
@ConfigurationProperties(prefix = "keycloak.admin")
public class KeycloakAdminProperties {

  @NotBlank
  private String serverUrl;

  @NotBlank
  private String realm;

  @NotBlank
  private String clientId;

  /**
   * Marcado @NotBlank pero lo validamos manualmente al construir el bean
   * para dar un error más claro que el de Bean Validation.
   */
  private String clientSecret;

  @Min(1000)
  private int connectTimeoutMillis = 5000;

  @Min(1000)
  private int readTimeoutMillis = 10000;

  public String getServerUrl() { return serverUrl; }
  public void setServerUrl(String serverUrl) { this.serverUrl = serverUrl; }

  public String getRealm() { return realm; }
  public void setRealm(String realm) { this.realm = realm; }

  public String getClientId() { return clientId; }
  public void setClientId(String clientId) { this.clientId = clientId; }

  public String getClientSecret() { return clientSecret; }
  public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

  public int getConnectTimeoutMillis() { return connectTimeoutMillis; }
  public void setConnectTimeoutMillis(int connectTimeoutMillis) { this.connectTimeoutMillis = connectTimeoutMillis; }

  public int getReadTimeoutMillis() { return readTimeoutMillis; }
  public void setReadTimeoutMillis(int readTimeoutMillis) { this.readTimeoutMillis = readTimeoutMillis; }
}
