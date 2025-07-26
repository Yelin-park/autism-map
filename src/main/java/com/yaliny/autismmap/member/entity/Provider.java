package com.yaliny.autismmap.member.entity;

import lombok.Getter;

@Getter
public enum Provider {
    GOOGLE("구글"),
    KAKAO("카카오");

    private final String description;

    Provider(String description) {
        this.description = description;
    }

    public static Provider getProvider(String providerName) {
        return switch (providerName.toUpperCase()) {
            case "GOOGLE" -> GOOGLE;
            case "KAKAO" -> KAKAO;
            default -> throw new IllegalArgumentException("Unknown provider: " + providerName);
        };
    }

}
