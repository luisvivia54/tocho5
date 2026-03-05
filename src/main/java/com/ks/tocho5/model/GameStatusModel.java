// src/main/java/com/ks/tocho5/model/GameStatusModel.java
package com.ks.tocho5.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "game")
public class GameStatusModel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "game_id")
  private Integer game_id;

  @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "season_id")
  private SeasonModel season;

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

  @Column(name = "round_label")
  private String roundLabel;

  // ✅ NUEVO: cancha/sede (PERSISTE en game.venue)
  @Column(name = "venue")
  private String venue;

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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "category_id",
      referencedColumnName = "category_id",
      insertable = false,
      updatable = false
  )
  private CategoryModel category;

  // ===== Getters / Setters =====

  public Integer getGame_id() { return game_id; }
  public void setGame_id(Integer game_id) { this.game_id = game_id; }

  public SeasonModel getSeason() { return season; }
  public void setSeason(SeasonModel season) { this.season = season; }

  public Integer getCategory_id() { return category_id; }
  public void setCategory_id(Integer category_id) { this.category_id = category_id; }

  public Integer getHome_team_id() { return home_team_id; }
  public void setHome_team_id(Integer home_team_id) { this.home_team_id = home_team_id; }

  public Integer getAway_team_id() { return away_team_id; }
  public void setAway_team_id(Integer away_team_id) { this.away_team_id = away_team_id; }

  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }

  public LocalDateTime getMatch_date_utc() { return match_date_utc; }
  public void setMatch_date_utc(LocalDateTime match_date_utc) { this.match_date_utc = match_date_utc; }

  public String getRoundLabel() { return roundLabel; }
  public void setRoundLabel(String roundLabel) { this.roundLabel = roundLabel; }

  // ✅ venue
  public String getVenue() { return venue; }
  public void setVenue(String venue) { this.venue = venue; }

  public LocalDateTime getUpdated_at() { return updated_at; }
  public void setUpdated_at(LocalDateTime updated_at) { this.updated_at = updated_at; }

  public EquiposModel getHomeTeam() { return homeTeam; }
  public void setHomeTeam(EquiposModel homeTeam) { this.homeTeam = homeTeam; }

  public EquiposModel getAwayTeam() { return awayTeam; }
  public void setAwayTeam(EquiposModel awayTeam) { this.awayTeam = awayTeam; }

  public CategoryModel getCategory() { return category; }
  public void setCategory(CategoryModel category) { this.category = category; }

  // ---- Getters “de nombre” (para JSON bonito) ----
  @com.fasterxml.jackson.annotation.JsonProperty("home_team")
  public String getHomeTeamName() {
    return homeTeam != null ? homeTeam.getName() : null;
  }

  @com.fasterxml.jackson.annotation.JsonProperty("away_team")
  public String getAwayTeamName() {
    return awayTeam != null ? awayTeam.getName() : null;
  }

  //================== SCORE (NO PERSISTE EN game) ==================
  @Transient
  private Integer homeScore;

  @Transient
  private Integer awayScore;

  public Integer getHomeScore() { return homeScore; }
  public void setHomeScore(Integer homeScore) { this.homeScore = homeScore; }

  public Integer getAwayScore() { return awayScore; }
  public void setAwayScore(Integer awayScore) { this.awayScore = awayScore; }

  @Override
  public String toString() {
    return "GameStatusModel [game_id=" + game_id
        + ", season_id=" + season
        + ", category_id=" + category_id
        + ", home_team_id=" + home_team_id
        + ", away_team_id=" + away_team_id
        + ", status=" + status
        + ", match_date_utc=" + match_date_utc
        + ", round_label=" + roundLabel
        + ", venue=" + venue
        + "]";
  }
}