package com.yaliny.autismmap.member.entity;

import lombok.Getter;

@Getter
public enum Provider {
    GOOGLE("구글");

    private final String description;

    Provider(String description) {
        this.description = description;
    }

}
