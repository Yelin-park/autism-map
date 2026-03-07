package com.yaliny.autismmap.place.entity;

import lombok.Getter;

@Getter
public enum CrowdLevel {
    CROWDED("혼잡"),
    NORMAL("보통"),
    QUIET("한산");

    private final String description;

    CrowdLevel(String description) {
        this.description = description;
    }

    public static CrowdLevel from(int level) {
        return switch (level) {
            case 1 -> QUIET;
            case 2 -> QUIET;
            case 3 -> NORMAL;
            case 4 -> CROWDED;
            case 5 -> CROWDED;
            default -> throw new IllegalArgumentException("Invalid level: " + level);
        };
    }
}
