// src/main/java/com/ks/tocho5/dto/team/UpdateTeamRequest.java
package com.ks.tocho5.model;

import jakarta.validation.constraints.NotBlank;

public class UpdateTeamRequest {

    @NotBlank
    private String name;

    private String shortName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
}
