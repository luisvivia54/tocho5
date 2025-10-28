package com.ks.tocho5.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "game_score", schema = "public")
public class StandingTeamModel{
	@Id
	private Integer standing_id;
	private Integer season_id;
	private Integer category_id;
	private Integer team_id;
	
}