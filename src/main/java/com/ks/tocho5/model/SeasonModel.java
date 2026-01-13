package com.ks.tocho5.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "season")
public class SeasonModel {
	
	@Id
	@Column(name = "season_id")
	private Integer season_id;
	
	@Column(name = "league_id")
	private Integer league_id;
	
	@Column(name = "league_id")
	private String name;

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
	 * @return the league_id
	 */
	public Integer getLeague_id() {
		return league_id;
	}

	/**
	 * @param league_id the league_id to set
	 */
	public void setLeague_id(Integer league_id) {
		this.league_id = league_id;
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
		return "SeasonModel [season_id=" + season_id + ", league_id=" + league_id + ", name=" + name + "]";
	}
	
	
}
