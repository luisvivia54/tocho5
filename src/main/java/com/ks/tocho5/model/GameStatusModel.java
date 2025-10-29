package com.ks.tocho5.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "game")
public class GameStatusModel{
	/**
	 * @return the game_id
	 */
	public Integer getGame_id() {
		return game_id;
	}
	/**
	 * @param game_id the game_id to set
	 */
	public void setGame_id(Integer game_id) {
		this.game_id = game_id;
	}
	/**
	 * @return the season_id
	 */
	public Integer getSeason_id() {
		return season_id;
	}
	/**
	 * @param season_id the season_id to set
	 */
	public void setSeason_id(Integer season_id) {
		this.season_id = season_id;
	}
	/**
	 * @return the category_id
	 */
	public Integer getCategory_id() {
		return category_id;
	}
	/**
	 * @param category_id the category_id to set
	 */
	public void setCategory_id(Integer category_id) {
		this.category_id = category_id;
	}
	/**
	 * @return the home_team_id
	 */
	public Integer getHome_team_id() {
		return home_team_id;
	}
	/**
	 * @param home_team_id the home_team_id to set
	 */
	public void setHome_team_id(Integer home_team_id) {
		this.home_team_id = home_team_id;
	}
	/**
	 * @return the away_team_id
	 */
	public Integer getAway_team_id() {
		return away_team_id;
	}
	/**
	 * @param away_team_id the away_team_id to set
	 */
	public void setAway_team_id(Integer away_team_id) {
		this.away_team_id = away_team_id;
	}
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	@Id
	private Integer game_id;
	@Override
	public String toString() {
		return "GameStatusModel [game_id=" + game_id + ", season_id=" + season_id + ", category_id=" + category_id
				+ ", home_team_id=" + home_team_id + ", away_team_id=" + away_team_id + ", status=" + status + "]";
	}
	private Integer season_id;
	private Integer category_id;
	private Integer home_team_id;
	private Integer away_team_id;
	private String status;
	  // ===== Relaciones para leer nombres =====
	  @ManyToOne(fetch = FetchType.LAZY)
	  @JoinColumn(name = "home_team_id", referencedColumnName = "team_id",
	              insertable = false, updatable = false)
	  private EquiposModel homeTeam;

	  @ManyToOne(fetch = FetchType.LAZY)
	  @JoinColumn(name = "away_team_id", referencedColumnName = "team_id",
	              insertable = false, updatable = false)
	  private EquiposModel awayTeam;

	  // ---- Getters “de nombre” (para JSON bonito) ----
	  @com.fasterxml.jackson.annotation.JsonProperty("home_team")
	  public String getHomeTeamName() {
	    return homeTeam != null ? homeTeam.getName() : null;
	  }

	  @com.fasterxml.jackson.annotation.JsonProperty("away_team")
	  public String getAwayTeamName() {
	    return awayTeam != null ? awayTeam.getName() : null;
	  }
	
	
}