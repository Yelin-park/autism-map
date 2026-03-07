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

    public static PlaceCategory from(String description) {
        return switch (description) {
            case "산책길" -> WALKING_PATH;
            case "음식점" -> RESTAURANT;
            case "카페" -> CAFE;
            case "숙소" -> LODGING;
            case "가볼만한 곳" -> ATTRACTION;
            case "기타" -> ETC;
            default -> throw new IllegalArgumentException("Invalid description: " + description);
        };
    }
}
