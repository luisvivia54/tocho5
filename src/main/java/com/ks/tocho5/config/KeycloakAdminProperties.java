package com.ks.tocho5.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades del Keycloak Admin Client.
 * Todos los valores vienen de application.properties (resueltos desde ENV).
 * La validación se hace en KeycloakAdminConfig para poder fallar con mensajes
 * claros sin tumbar el arranque completo si solo falta el secret.
 */
@ConfigurationProperties(prefix = "keycloak.admin")
public class KeycloakAdminProperties {

  private String serverUrl;
  private String realm;
  private String clientId;
  private String clientSecret;
  private int connectTimeoutMillis = 5000;
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
