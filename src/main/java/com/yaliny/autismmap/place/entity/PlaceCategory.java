package com.yaliny.autismmap.place.entity;

import lombok.Getter;

@Getter
public enum PlaceCategory {
    WALKING_PATH("산책길"),
    RESTAURANT("음식점"),
    CAFE("카페"),
    LODGING("숙소"),
    ATTRACTION("가볼만한 곳"),
    ETC("기타");

    private final String description;

    PlaceCategory(String description) {
        this.description = description;
    }
}
