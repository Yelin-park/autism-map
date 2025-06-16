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
}
