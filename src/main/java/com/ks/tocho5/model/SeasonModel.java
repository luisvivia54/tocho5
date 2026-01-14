package com.ks.tocho5.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Table(name = "season")
public class SeasonModel {
	
	@Id
	@Column(name = "season_id")
	private Long seasonId;
	
	@Column(name = "league_id")
	private Long leagueId;
	
	@Column(name = "name")
	private String name;

	/**
	 * @return the season_id
	 */
	public Long getSeasonId() {
		return seasonId;
	}

	/**
	 * @param season_id the season_id to set
	 */
	public void setSeasonId(Long seasonId) {
		this.seasonId = seasonId;
	}

	/**
	 * @return the league_id
	 */
	public Long getLeagueId() {
		return leagueId;
	}

	/**
	 * @param league_id the league_id to set
	 */
	public void setLeagueId(Long leagueId) {
		this.leagueId = leagueId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "SeasonModel [season_id=" + seasonId + ", league_id=" + leagueId + ", name=" + name + "]";
	}
	
	
}
