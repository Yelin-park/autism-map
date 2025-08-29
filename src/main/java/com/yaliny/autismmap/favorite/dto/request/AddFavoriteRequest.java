package com.yaliny.autismmap.favorite.dto.request;

public record AddFavoriteRequest(
    Long memberId,
    Long placeId
) {
}
