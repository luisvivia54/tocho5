// src/main/java/com/ks/tocho5/model/GameStatusModel.java
package com.ks.tocho5.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "game")
public class GameStatusModel {

  @Id
  @Column(name = "game_id")
  private Integer game_id;

  @Column(name = "season_id")
  private Integer season_id;

  @Column(name = "category_id")
  private Integer category_id;

  @Column(name = "home_team_id")
  private Integer home_team_id;

  @Column(name = "away_team_id")
  private Integer away_team_id;

  @Column(name = "status")
  private String status;

  @Column(name = "match_date_utc")
  private LocalDateTime match_date_utc;

  // OJO: en tu repo usas g.round_la, por eso debe existir el atributo
  @Column(name = "round_label")
  private String round_la;

  // opcional
  @Column(name = "updated_at")
  private LocalDateTime updated_at;

  // ===== Relaciones para leer nombres =====

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "home_team_id",
      referencedColumnName = "team_id",
      insertable = false,
      updatable = false
  )
  private EquiposModel homeTeam;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "away_team_id",
      referencedColumnName = "team_id",
      insertable = false,
      updatable = false
  )
  private EquiposModel awayTeam;

  // ✅ ESTO arregla el error: "Could not resolve attribute 'category'"
  // Si tu clase se llama diferente, cambia SOLO el tipo (CategoryModel).
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "category_id",
      referencedColumnName = "category_id",
      insertable = false,
      updatable = false
  )
  private CategoryModel category;

  // ===== Getters / Setters =====

  public Integer getGame_id() {
    return game_id;
  }

  public void setGame_id(Integer game_id) {
    this.game_id = game_id;
  }

  public Integer getSeason_id() {
    return season_id;
  }

  public void setSeason_id(Integer season_id) {
    this.season_id = season_id;
  }

  public Integer getCategory_id() {
    return category_id;
  }

  public void setCategory_id(Integer category_id) {
    this.category_id = category_id;
  }

  public Integer getHome_team_id() {
    return home_team_id;
  }

  public void setHome_team_id(Integer home_team_id) {
    this.home_team_id = home_team_id;
  }

  public Integer getAway_team_id() {
    return away_team_id;
  }

  public void setAway_team_id(Integer away_team_id) {
    this.away_team_id = away_team_id;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public LocalDateTime getMatch_date_utc() {
    return match_date_utc;
  }

  public void setMatch_date_utc(LocalDateTime match_date_utc) {
    this.match_date_utc = match_date_utc;
  }

  public String getRound_la() {
    return round_la;
  }

  public void setRound_la(String round_la) {
    this.round_la = round_la;
  }

  public LocalDateTime getUpdated_at() {
    return updated_at;
  }

  public void setUpdated_at(LocalDateTime updated_at) {
    this.updated_at = updated_at;
  }

  public EquiposModel getHomeTeam() {
    return homeTeam;
  }

  public void setHomeTeam(EquiposModel homeTeam) {
    this.homeTeam = homeTeam;
  }

  public EquiposModel getAwayTeam() {
    return awayTeam;
  }

  public void setAwayTeam(EquiposModel awayTeam) {
    this.awayTeam = awayTeam;
  }

  public CategoryModel getCategory() {
    return category;
  }

  public void setCategory(CategoryModel category) {
    this.category = category;
  }

  // ---- Getters “de nombre” (para JSON bonito) ----
  @com.fasterxml.jackson.annotation.JsonProperty("home_team")
  public String getHomeTeamName() {
    return homeTeam != null ? homeTeam.getName() : null;
  }

  @com.fasterxml.jackson.annotation.JsonProperty("away_team")
  public String getAwayTeamName() {
    return awayTeam != null ? awayTeam.getName() : null;
  }

  @Override
  public String toString() {
    return "GameStatusModel [game_id=" + game_id
        + ", season_id=" + season_id
        + ", category_id=" + category_id
        + ", home_team_id=" + home_team_id
        + ", away_team_id=" + away_team_id
        + ", status=" + status
        + ", match_date_utc=" + match_date_utc
        + ", round_la=" + round_la
        + "]";
  }
}
