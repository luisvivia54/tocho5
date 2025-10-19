package com.ks.tocho5.model;

// Los nombres de métodos deben coincidir con los alias del SELECT
public interface EquiposProjection {
  Integer getTeam_id();
  String  getName();
  Integer getCategory_id();
  Integer getSeason_id();
}
