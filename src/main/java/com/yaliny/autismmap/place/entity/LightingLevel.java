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

}
