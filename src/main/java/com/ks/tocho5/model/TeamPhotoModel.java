package com.ks.tocho5.model;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "team_media")
public class TeamPhotoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long media_id;

    // Relación con equipos
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private EquiposModel team;

    @Column(name = "image_url", nullable = false, length = 1000)
    private String photoUrl;

    @Column(name = "slot", nullable = false)
    private Integer sortOrder;


    // Getters / setters

    public Long getId() { return media_id; }

    public EquiposModel getTeam() { return team; }
    public void setTeam(EquiposModel team) { this.team = team; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

}
