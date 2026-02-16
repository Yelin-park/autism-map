package com.yaliny.autismmap.place.entity;

import lombok.Getter;

@Getter
public enum NoiseLevel {
    VERY_QUIET(1, "매우 조용함"),
    QUIET(2, "조용함"),
    MODERATE(3, "보통"),
    NOISY(4, "시끄러움"),
    VERY_NOISY(5, "매우 시끄러움");

    private final int level;
    private final String description;

    NoiseLevel(int level, String description) {
        this.level = level;
        this.description = description;
    }
}
