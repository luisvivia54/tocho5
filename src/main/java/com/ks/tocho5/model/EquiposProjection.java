package com.ks.tocho5.model;

// Los nombres de métodos deben coincidir con los alias del SELECT
public interface EquiposProjection {
  Integer getTeamId();
  String  getName();
  Integer getCategoryId();
  Integer getSeasonId();
}
