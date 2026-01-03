package com.ks.tocho5.model;

public interface PlayerSeasonStatsProjection {
    Long getPlayerId();
    String getFullName();
    Integer getTd();
    Integer getPassTd();
    Integer getIntercep();
    Integer getSacks();
}
