package com.ks.tocho5.model;

import java.util.Locale;

public class CategoryDto {

    private Long id;
    private Long leagueId;
    private String name;
    private String code;
    private String gender;

    // Division dentro de la rama: "A" | "B". null cuando la categoria no esta dividida.
    private String levelOrder;
    private Boolean isActive;

    // name + division, listo para pintar: "Varonil A", "Varonil B", "Varonil".
    private String displayName;

    public CategoryDto(Long id, Long leagueId, String name, String code, String gender,
                       String levelOrder, Boolean isActive) {
        this.id = id;
        this.leagueId = leagueId;
        this.name = name;
        this.code = code;
        this.gender = gender;
        this.levelOrder = normalizeLevelOrder(levelOrder);
        this.isActive = isActive;
        this.displayName = buildDisplayName(name, this.levelOrder);
    }

    /**
     * En BD "0" significa "sin division". Lo convertimos a null para que el front
     * pueda preguntar simplemente si levelOrder viene o no.
     */
    private static String normalizeLevelOrder(String raw) {
        if (raw == null) return null;
        String value = raw.trim();
        if (value.isEmpty() || "0".equals(value)) return null;
        return value.toUpperCase(Locale.ROOT);
    }

    private static String buildDisplayName(String name, String normalizedLevelOrder) {
        String base = (name == null) ? "" : name.trim();
        if (normalizedLevelOrder == null) return base;
        if (base.isEmpty()) return normalizedLevelOrder;
        return base + " " + normalizedLevelOrder;
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

    public String getLevelOrder() {
        return levelOrder;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public String getDisplayName() {
        return displayName;
    }
}
