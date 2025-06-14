package com.yaliny.autismmap.place.dto.response;

import com.yaliny.autismmap.place.entity.Place;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

public record PlaceListResponse(
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
    @Schema(title = "장소 목록")
    List<PlaceDetailResponse> content
) {
    public static PlaceListResponse of(Page<Place> page) {
        return new PlaceListResponse(
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast(),
            page.getContent().stream().map(PlaceDetailResponse::of).toList()
        );
    }
}
