package com.ks.tocho5.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "standing_team")
public class StandingTeamModel{
	/**
	 * @return the standing_id
	 */
	public Integer getStanding_id() {
		return standing_id;
	}
	/**
	 * @param standing_id the standing_id to set
	 */
	public void setStanding_id(Integer standing_id) {
		this.standing_id = standing_id;
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
	 * @return the team_id
	 */
	public Integer getTeam_id() {
		return team_id;
	}
	/**
	 * @param team_id the team_id to set
	 */
	public void setTeam_id(Integer team_id) {
		this.team_id = team_id;
	}
	/**
	 * @return the gp
	 */
	public Integer getGp() {
		return gp;
	}
	/**
	 * @param gp the gp to set
	 */
	public void setGp(Integer gp) {
		this.gp = gp;
	}
	/**
	 * @return the wins
	 */
	public Integer getWins() {
		return wins;
	}
	/**
	 * @param wins the wins to set
	 */
	public void setWins(Integer wins) {
		this.wins = wins;
	}
	/**
	 * @return the losses
	 */
	public Integer getLosses() {
		return losses;
	}
	/**
	 * @param losses the losses to set
	 */
	public void setLosses(Integer losses) {
		this.losses = losses;
	}
	/**
	 * @return the draws
	 */
	public Integer getDraws() {
		return draws;
	}
	/**
	 * @param draws the draws to set
	 */
	public void setDraws(Integer draws) {
		this.draws = draws;
	}
	/**
	 * @return the points_for
	 */
	public Integer getPoints_for() {
		return points_for;
	}
	/**
	 * @param points_for the points_for to set
	 */
	public void setPoints_for(Integer points_for) {
		this.points_for = points_for;
	}
	/**
	 * @return the points_against
	 */
	public Integer getPoints_against() {
		return points_against;
	}
	/**
	 * @param points_against the points_against to set
	 */
	public void setPoints_against(Integer points_against) {
		this.points_against = points_against;
	}
	/**
	 * @return the table_points
	 */
	public Integer getTable_points() {
		return table_points;
	}
	/**
	 * @param table_points the table_points to set
	 */
	public void setTable_points(Integer table_points) {
		this.table_points = table_points;
	}
	@Id
	private Integer standing_id;
	@Override
	public String toString() {
		return "StandingTeamModel [standing_id=" + standing_id + ", season_id=" + season_id + ", category_id="
				+ category_id + ", team_id=" + team_id + ", gp=" + gp + ", wins=" + wins + ", losses=" + losses
				+ ", draws=" + draws + ", points_for=" + points_for + ", points_against=" + points_against
				+ ", table_points=" + table_points + "]";
	}
	private Integer season_id;
	private Integer category_id;
	private Integer team_id;
	private Integer gp;
	private Integer wins;
	private Integer losses;
	private Integer draws;
	private Integer points_for;
	private Integer points_against;
	private Integer table_points;
}