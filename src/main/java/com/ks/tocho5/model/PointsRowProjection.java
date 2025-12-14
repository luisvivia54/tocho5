package com.ks.tocho5.model;

public interface PointsRowProjection {
    Integer getStandingId();
    Integer getSeasonId();
    Integer getCategoryId();
    Integer getTeamId();

    Integer getGp();
    Integer getWins();
    Integer getLosses();
    Integer getDraws();

    Integer getPointsFor();
    Integer getPointsAgainst();
    Integer getTablePoints();

    String getTeamName();

    // ✅ extras para UI
    String getCategoryCode();
    String getGender();
}
