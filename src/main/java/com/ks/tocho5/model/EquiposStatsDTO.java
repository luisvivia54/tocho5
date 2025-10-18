package com.ks.tocho5.model;

public class EquiposStatsDTO {
  public Integer teamId;
  public String  name;
  public Integer categoryId;
  public Integer seasonId;

  public EquiposStatsDTO(Integer teamId, String name, Integer categoryId, Integer seasonId) {
    this.teamId = teamId;
    this.name = name;
    this.categoryId = categoryId;
    this.seasonId = seasonId;
  }
}
