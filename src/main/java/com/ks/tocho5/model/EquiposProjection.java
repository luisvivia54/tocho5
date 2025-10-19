package com.ks.tocho5.model;

// Los nombres de métodos deben coincidir con los alias del SELECT
public interface EquiposProjection {
  Integer getteam_id();
  String  getname();
  Integer getcategory_id();
  Integer getseason_id();
}
