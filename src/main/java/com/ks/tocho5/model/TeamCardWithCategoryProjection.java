package com.ks.tocho5.model;

public interface TeamCardWithCategoryProjection {
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
    String getCategoryCode();     // rama (U16, U18, etc)
    String getCategoryGender();   // categoria (FEMENIL, VARONIL, MIXTO)
}
