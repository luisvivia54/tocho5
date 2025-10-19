package com.ks.tocho5.model;

import com.fasterxml.jackson.annotation.JsonInclude;

//datos entrada
@JsonInclude(JsonInclude.Include.NON_NULL) // al serializar, omite llaves null
public class TeamStatsFilterDTO {
	public String name;       // filtro por nombre contiene
	  public Integer category_id;
	  public Integer season_id;
	  public Integer team_id;

	  // paginación simple
	  public Integer page;   // default 0
	  public Integer size;   // default 20
}
