package com.ks.tocho5.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class GameModel{
	@Override
	public String toString() {
		return "DatoValorDTO [local_team=" + local_team + ", away_team=" + away_team + ", status=" + status + ", game_id="+ game_id+ "]";
	}
	private Integer local_team;
	private Integer away_team;
	@Id
	private Integer game_id;
	private String status;
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
	public Integer getLocal_team() {
		return local_team;
	}
	/**
	 * @param local_team the local_team to set
	 */
	public void setLocal_team(Integer local_team) {
		this.local_team = local_team;
	}
	/**
	 * @return the away_team
	 */
	public Integer getAway_team() {
		return away_team;
	}
	/**
	 * @param away_team the away_team to set
	 */
	public void setAway_team(Integer away_team) {
		this.away_team = away_team;
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
	
	
}