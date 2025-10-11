package com.ks.tocho5.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "team")
public class EquiposModel {

  // =========================
  // Campos (variables al inicio)
  // =========================

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT en MySQL
  @Column(name = "team_id", nullable = false)
  private Integer teamId;

  @Column(name = "league_id", nullable = false)
  private Integer leagueId;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "short_name")
  private String shortName;

  @Column(name = "color_primary")
  private String colorPrimary;

  @Column(name = "color_secondary")
  private String colorSecondary;

  @Column(name = "logo_url", length = 255)
  private String logoUrl;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true; // mapea TINYINT(1) a boolean

  // En tu DDL son TIMESTAMP con default CURRENT_TIMESTAMP
  // Si quieres formateo JSON:
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "created_at", insertable = false, updatable = false)
  private LocalDateTime createdAt;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "updated_at", insertable = false, updatable = false)
  private LocalDateTime updatedAt;

  // =========================
  // Constructores
  // =========================

  public EquiposModel() {
  }

  public EquiposModel(
      Integer teamId,
      Integer leagueId,
      String name,
      String shortName,
      String colorPrimary,
      String colorSecondary,
      String logoUrl,
      Boolean isActive,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.teamId = teamId;
    this.leagueId = leagueId;
    this.name = name;
    this.shortName = shortName;
    this.colorPrimary = colorPrimary;
    this.colorSecondary = colorSecondary;
    this.logoUrl = logoUrl;
    this.isActive = isActive;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  // =========================
  // Getters / Setters
  // =========================

  public Integer getTeamId() {
    return teamId;
  }

  public void setTeamId(Integer teamId) {
    this.teamId = teamId;
  }

  public Integer getLeagueId() {
    return leagueId;
  }

  public void setLeagueId(Integer leagueId) {
    this.leagueId = leagueId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getShortName() {
    return shortName;
  }

  public void setShortName(String shortName) {
    this.shortName = shortName;
  }

  public String getColorPrimary() {
    return colorPrimary;
  }

  public void setColorPrimary(String colorPrimary) {
    this.colorPrimary = colorPrimary;
  }

  public String getColorSecondary() {
    return colorSecondary;
  }

  public void setColorSecondary(String colorSecondary) {
    this.colorSecondary = colorSecondary;
  }

  public String getLogoUrl() {
    return logoUrl;
  }

  public void setLogoUrl(String logoUrl) {
    this.logoUrl = logoUrl;
  }

  public Boolean getIsActive() {
    return isActive;
  }

  public void setIsActive(Boolean isActive) {
    this.isActive = isActive;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  // =========================
  // Opcional: toString/equals/hashCode
  // =========================
  @Override
  public String toString() {
    return "EquiposModel{" +
        "teamId=" + teamId +
        ", leagueId=" + leagueId +
        ", name='" + name + '\'' +
        ", shortName='" + shortName + '\'' +
        ", colorPrimary='" + colorPrimary + '\'' +
        ", colorSecondary='" + colorSecondary + '\'' +
        ", logoUrl='" + logoUrl + '\'' +
        ", isActive=" + isActive +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        '}';
  }
}
