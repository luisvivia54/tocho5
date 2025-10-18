package com.ks.tocho5.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "team_enrollment")
public class EquiposStatsModel {


	  /**
	 * @return the enrollment_id
	 */
	public Integer getEnrollment_id() {
		return enrollment_id;
	}

	/**
	 * @param enrollment_id the enrollment_id to set
	 */
	public void setEnrollment_id(Integer enrollment_id) {
		this.enrollment_id = enrollment_id;
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

	@Id
	  @GeneratedValue(strategy = GenerationType.IDENTITY) // ajusta si no es autoincrement
	  @Column(name = "enrollment_id")
	  private Integer enrollment_id;

	  @Override
	public String toString() {
		return "EquiposStatsModel [enrollment_id=" + enrollment_id + ", team_id=" + team_id + ", category_id="
				+ category_id + ", season_id=" + season_id + "]";
	}

	@Column(name = "team_id")
	  private Integer team_id;

	  @Column(name = "category_id")
	  private Integer category_id;

	  @Column(name = "season_id")
	  private Integer season_id;
}
