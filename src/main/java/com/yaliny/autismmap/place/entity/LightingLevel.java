package com.yaliny.autismmap.place.entity;

import lombok.Getter;

@Getter
public enum LightingLevel {
    BRIGHT("밝음"),
    MODERATE("적당함"),
    DARK("어두움");

    private final String description;

    LightingLevel(String description) {
        this.description = description;
    }

    public static LightingLevel from(int level) {
        return switch (level) {
            case 1 -> DARK;
            case 2 -> DARK;
            case 3 -> MODERATE;
            case 4 -> BRIGHT;
            case 5 -> BRIGHT;
            default -> throw new IllegalArgumentException("Invalid level: " + level);
        };
    }

}
