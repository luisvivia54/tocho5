package com.ks.tocho5.model;

public interface TeamListProjection {

    Integer getTeamId();
    Integer getLeagueId();

    String getName();
    String getShortName();
    String getColorPrimary();
    String getColorSecondary();
    String getLogoUrl();
    Boolean getIsActive();

    Integer getSeasonId();
    Integer getCategoryId();
    String  getCategoryName();
    String  getCategoryCode();
    String  getCategoryGender();
}
