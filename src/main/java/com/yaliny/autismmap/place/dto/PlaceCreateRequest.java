package com.yaliny.autismmap.place.dto;

import com.yaliny.autismmap.place.entity.CrowdLevel;
import com.yaliny.autismmap.place.entity.LightingLevel;
import com.yaliny.autismmap.place.entity.PlaceCategory;
import io.swagger.v3.oas.annotations.media.Schema;

public record PlaceCreateRequest(
    @Schema(title = "장소명", description = "장소명", example = "아쿠아리움")
    String name,
    @Schema(title = "설명", description = "설명", example = "물고기를 많이 볼 수 있는 장소")
    String description,
    @Schema(title = "장소 카테고리 구분", example = "RESTAURANT")
    PlaceCategory category,
    String address,
    Double latitude,
    Double longitude,
    boolean isWheelchairFriendly,
    boolean isQuiet,
    boolean hasParking,
    boolean hasRestArea,
    boolean hasPrivateRoom,
    LightingLevel lightingLevel,
    CrowdLevel crowdLevel,
    @Schema(title = "영업 시작 시간", description = "24시간 형태로 작성해 주세요.", example = "09:00")
    String businessStartTime,
    @Schema(title = "영업 종료 시간", description = "24시간 형태로 작성해 주세요.", example = "18:30")
    String businessClosingTime,
    String dayOff
) {
}
