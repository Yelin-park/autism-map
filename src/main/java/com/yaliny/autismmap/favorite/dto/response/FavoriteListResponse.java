package com.yaliny.autismmap.favorite.dto.response;

import com.yaliny.autismmap.favorite.entity.Favorite;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

public record FavoriteListResponse(
    @Schema(title = "현재 페이지")
    int page,
    @Schema(title = "페이지 크기")
    int size,
    @Schema(title = "총 항목 수")
    long totalElements,
    @Schema(title = "총 페이지 수")
    int totalPages,
    @Schema(title = "마지막 페이지 여부")
    boolean last,
    @Schema(title = "즐겨찾기 목록")
    List<FavoriteCardResponse> content
) {
    public static FavoriteListResponse of(Page<Favorite> page) {
        return new FavoriteListResponse(
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast(),
            page.getContent().stream().map(FavoriteCardResponse::of).toList()
        );
    }
}
