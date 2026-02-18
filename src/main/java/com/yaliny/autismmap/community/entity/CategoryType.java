package com.yaliny.autismmap.community.entity;

import lombok.Getter;

@Getter
public enum CategoryType {

    QNA("질문&답변"),
    INFO_SHARE("꿀팁/정보"),
    FREE("자유게시판");

    private final String description;

    CategoryType(String description) {
        this.description = description;
    }

    public static CategoryType getCategoryType(String categoryName) {
        return switch (categoryName.toUpperCase()) {
            case "QNA" -> QNA;
            case "INFO_SHARE" -> INFO_SHARE;
            case "FREE" -> FREE;
            default -> throw new IllegalArgumentException("Unknown Category: " + categoryName);
        };
    }
}
