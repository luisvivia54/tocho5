package com.ks.tocho5.model;

public class CategoryDto {

    private Long id;
    private Long leagueId;
    private String name;
    private String code;
    private String gender;

    public CategoryDto(Long id, Long leagueId, String name, String code, String gender) {
        this.id = id;
        this.leagueId = leagueId;
        this.name = name;
        this.code = code;
        this.gender = gender;
    }

    public Long getId() {
        return id;
    }

    public Long getLeagueId() {
        return leagueId;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getGender() {
        return gender;
    }
}
