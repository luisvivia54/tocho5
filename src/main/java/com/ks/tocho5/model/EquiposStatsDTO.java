package com.ks.tocho5.model;

public class EquiposStatsDTO {
  public Integer team_id;
  public String  name;
  public Integer category_id;
  public Integer season_id;

  public EquiposStatsDTO(Integer team_id, String name, Integer category_id, Integer season_id) {
    this.team_id = team_id;
    this.name = name;
    this.category_id = category_id;
    this.season_id = season_id;
  }
}
