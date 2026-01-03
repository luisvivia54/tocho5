package com.ks.tocho5.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "stat_player_game")
public class StatPlayerGameModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stat_player_game_id", nullable = false)
    private Long statPlayerGameId;

    @Column(name = "game_id", nullable = false)
    private Long gameId;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "player_id", nullable = false)
    private Long playerId;

    // ✅ Solo las 4 que vas a usar:
    @Column(name = "td")
    private Integer td;

    @Column(name = "pass_td")
    private Integer passTd;

    @Column(name = "intercep")
    private Integer intercep;

    @Column(name = "sacks")
    private Integer sacks;

    // (no la tocamos, solo que exista)
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void touch() {
        this.updatedAt = OffsetDateTime.now();
    }

    public StatPlayerGameModel() {}

    // getters/setters
    public Long getStatPlayerGameId() { return statPlayerGameId; }

    public Long getGameId() { return gameId; }
    public void setGameId(Long gameId) { this.gameId = gameId; }

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }

    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }

    public Integer getTd() { return td; }
    public void setTd(Integer td) { this.td = td; }

    public Integer getPassTd() { return passTd; }
    public void setPassTd(Integer passTd) { this.passTd = passTd; }

    public Integer getIntercep() { return intercep; }
    public void setIntercep(Integer intercep) { this.intercep = intercep; }

    public Integer getSacks() { return sacks; }
    public void setSacks(Integer sacks) { this.sacks = sacks; }
}
