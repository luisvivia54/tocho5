package com.ks.tocho5.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "team_enrollment")
public class EquiposStatsModel {

  // =========================
  // Campos (variables al inicio)
  // =========================

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
	 * @return the registered_at
	 */
	public LocalDateTime getRegistered_at() {
		return registered_at;
	}

	/**
	 * @param registered_at the registered_at to set
	 */
	public void setRegistered_at(LocalDateTime registered_at) {
		this.registered_at = registered_at;
	}

	/**
	 * @return the is_active
	 */
	public Boolean getIs_active() {
		return is_active;
	}

	/**
	 * @param is_active the is_active to set
	 */
	public void setIs_active(Boolean is_active) {
		this.is_active = is_active;
	}

@Override
	public String toString() {
		return "EquiposStatsModel [enrollment_id=" + enrollment_id + ", team_id=" + team_id + ", season_id=" + season_id
				+ ", category_id=" + category_id + ", registered_at=" + registered_at + ", is_active=" + is_active
				+ "]";
	}

@Id
  @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT en MySQL
  @Column(name = "enrollment_id", nullable = false)
  private Integer enrollment_id;

  @Column(name = "team_id", nullable = false)
  private Integer team_id;

  @Column(name = "season_id", nullable = false)
  private Integer season_id;

  @Column(name = "category_id")
  private Integer category_id;

  @Column(name = "registered_at")
  private LocalDateTime registered_at;

  @Column(name = "is_active")
  private Boolean is_active;
}
