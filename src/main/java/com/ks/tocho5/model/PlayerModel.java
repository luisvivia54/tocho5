// src/main/java/com/ks/tocho5/model/PlayerModel.java
package com.ks.tocho5.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(name = "player")
public class PlayerModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "player_id")
    private Long playerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "team_id", nullable = false)
    private EquiposModel team;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "curp", nullable = false, length = 18)
    private String curp;

    @Column(name = "photo_url", nullable = false)
    private String photoUrl;

    @Column(name = "jersey_number")
    private Integer jerseyNumber;

    @Column(name = "birth_date")
    private LocalDate birthdate;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    // ===== getters & setters =====

    public Long getPlayerId() {
        return playerId;
    }

    public EquiposModel getTeam() {
        return team;
    }

    public void setTeam(EquiposModel team) {
        this.team = team;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCurp() {
        return curp;
    }

    public void setCurp(String curp) {
        this.curp = curp;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Integer getJerseyNumber() {
        return jerseyNumber;
    }

    public void setJerseyNumber(Integer jerseyNumber) {
        this.jerseyNumber = jerseyNumber;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
