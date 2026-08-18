package com.ks.tocho5.model;

public interface PlayerSeasonStatsProjection {
    // Llave opaca de persona (hash de la CURP) para agrupar sin exponer el dato real
    String getPersonKey();
    Long getPlayerId();
    Long getTeamId();
    String getFullName();
    String getPhotoUrl();
    Integer getNumber();
    Integer getTd();
    Integer getPassTd();
    Integer getIntercep();
    Integer getSacks();
}
