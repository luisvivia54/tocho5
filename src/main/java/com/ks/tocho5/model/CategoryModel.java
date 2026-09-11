package com.ks.tocho5.model;

import jakarta.persistence.*;

@Entity
@Table(name = "category")
public class CategoryModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @Column(name = "league_id", nullable = false)
    private Long leagueId;

    @Column(name = "name", nullable = false)
    private String name;

    // Código corto: "+35", "U-16", etc.
    @Column(name = "code", nullable = false)
    private String code;

    // "VARONIL" | "FEMENIL" | "MIXTO" (o null)
    @Column(name = "gender")
    private String gender;

    // Division dentro de la rama: "A" | "B" para Libres, "0" para el resto.
    // Es VARCHAR en BD: NO usar para ordenar numericamente ni comparar con numeros.
    @Column(name = "level_order")
    private String levelOrder;

    @Column(name = "is_active")
    private Boolean isActive;

    public CategoryModel() {
    }

    // --- getters & setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLeagueId() {
        return leagueId;
    }

    public void setLeagueId(Long leagueId) {
        this.leagueId = leagueId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLevelOrder() {
        return levelOrder;
    }

    public void setLevelOrder(String levelOrder) {
        this.levelOrder = levelOrder;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
