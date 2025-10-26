package com.ks.tocho5.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "game_score", schema = "public")
public class GameModel{
	@Override
	public String toString() {
		return "DatoValorDTO [local_team=" + home_score + ", away_team=" + away_score + ", game_id="+ game_id+ "]";
	}
	private Integer home_score;
	private Integer away_score;
	@Id
	private Integer game_id;

	/**
	 * @return the local_team
	 */
	public Integer getGame_id() {
		return game_id;
	}
	/**
	 * @param local_team the local_team to set
	 */
	public void setGame_id(Integer game_id) {
		this.game_id = game_id;
	}
	public Integer getHome_score() {
		return home_score;
	}
	/**
	 * @param local_team the local_team to set
	 */
	public void setHome_score(Integer home_score) {
		this.home_score = home_score;
	}
	/**
	 * @return the away_team
	 */
	public Integer getAway_score() {
		return away_score;
	}
	/**
	 * @param away_score the away_team to set
	 */
	public void setAway_score(Integer away_score) {
		this.away_score = away_score;
	}
	/**
	 * @return the status
	 */

	
	
}