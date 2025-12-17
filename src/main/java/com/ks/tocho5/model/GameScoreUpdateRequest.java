package com.ks.tocho5.model;

public class GameScoreUpdateRequest {
    private Integer game_id;
    private Integer home_score;
    private Integer away_score;

    public Integer getGame_id() { return game_id; }
    public void setGame_id(Integer game_id) { this.game_id = game_id; }

    public Integer getHome_score() { return home_score; }
    public void setHome_score(Integer home_score) { this.home_score = home_score; }

    public Integer getAway_score() { return away_score; }
    public void setAway_score(Integer away_score) { this.away_score = away_score; }
}
