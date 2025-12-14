package com.ks.tocho5.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "category")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CategoryModel {

  @Id
  @Column(name = "category_id")
  private Integer category_id;

  // OJO: en tu query usas c.leagueId, c.code, c.gender
  @Column(name = "league_id")
  private Integer leagueId;

  @Column(name = "code")
  private String code;

  @Column(name = "gender")
  private String gender;

  @Column(name = "name")
  private String name;

  public Integer getCategory_id() { return category_id; }
  public void setCategory_id(Integer category_id) { this.category_id = category_id; }

  public Integer getLeagueId() { return leagueId; }
  public void setLeagueId(Integer leagueId) { this.leagueId = leagueId; }

  public String getCode() { return code; }
  public void setCode(String code) { this.code = code; }

  public String getGender() { return gender; }
  public void setGender(String gender) { this.gender = gender; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
}
