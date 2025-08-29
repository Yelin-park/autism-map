package com.yaliny.autismmap.favorite.dto.response;

import com.yaliny.autismmap.favorite.entity.Favorite;
import io.swagger.v3.oas.annotations.media.Schema;

public record FavoriteCardResponse(
    @Schema(title = "즐겨찾기 ID")
    Long id,
    @Schema(title = "장소 ID")
    Long placeId,
    @Schema(title = "장소명", description = "장소명", example = "아쿠아리움")
    String name,
    @Schema(title = "설명", description = "설명", example = "물고기를 많이 볼 수 있는 장소")
    String description,
    @Schema(title = "장소 카테고리 구분", example = "음식점")
    String category,
    @Schema(title = "행정 구역(도/특별시/광역시)", example = "경기도")
    String provinceName,
    @Schema(title = "시/군/구", example = "수원시")
    String districtName,
    @Schema(title = "주소", example = "경기도 수원시 권선구 금곡로 10번길 29")
    String address,
    @Schema(title = "조용한 환경 여부")
    boolean isQuiet,
    @Schema(title = "주차장 유무")
    boolean hasParking,
    @Schema(title = "쉴 수 있는 공간 여부")
    boolean hasRestArea,
    @Schema(title = "프라이빗 룸 여부")
    boolean hasPrivateRoom,
    @Schema(title = "조명 밝기 수준", example = "밝음")
    String lightingLevel,
    @Schema(title = "혼잡도 수준", example = "보통")
    String crowdLevel
) {
    public static FavoriteCardResponse of(Favorite favorite) {
        return new FavoriteCardResponse(
            favorite.getId(),
            favorite.getPlace().getId(),
            favorite.getPlace().getName(),
            favorite.getPlace().getDescription(),
            favorite.getPlace().getCategory().getDescription(),
            favorite.getPlace().getProvince().getName(),
            favorite.getPlace().getDistrict().getName(),
            favorite.getPlace().getAddress(),
            favorite.getPlace().isQuiet(),
            favorite.getPlace().isHasParking(),
            favorite.getPlace().isHasRestArea(),
            favorite.getPlace().isHasPrivateRoom(),
            favorite.getPlace().getLightingLevel().getDescription(),
            favorite.getPlace().getCrowdLevel().getDescription()
        );
    }
}
