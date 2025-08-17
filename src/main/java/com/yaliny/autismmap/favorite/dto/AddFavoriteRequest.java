package com.yaliny.autismmap.favorite.dto;

public record AddFavoriteRequest(
    Long memberId,
    Long placeId
) {
}
