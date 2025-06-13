package com.yaliny.autismmap.place.dto.response;

import com.yaliny.autismmap.place.entity.CrowdLevel;
import com.yaliny.autismmap.place.entity.LightingLevel;
import com.yaliny.autismmap.place.entity.Place;
import com.yaliny.autismmap.place.entity.PlaceCategory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.format.DateTimeFormatter;

public record PlaceDetailResponse(
    @Schema(title = "장소 ID")
    Long id,
    @Schema(title = "장소명", description = "장소명", example = "아쿠아리움")
    String name,
    @Schema(title = "설명", description = "설명", example = "물고기를 많이 볼 수 있는 장소")
    String description,
    @Schema(title = "장소 카테고리 구분", example = "RESTAURANT")
    PlaceCategory category,
    @Schema(title = "상세 주소", example = "경기도 수원시 권선구 금곡로 10번길 29")
    String address,
    @Schema(title = "위도", example = "38.25")
    Double latitude,
    @Schema(title = "경도", example = "48.25")
    Double longitude,
    @Schema(title = "휠체어 친화 여부")
    boolean isWheelchairFriendly,
    @Schema(title = "조용한 환경 여부")
    boolean isQuiet,
    @Schema(title = "주차장 유무")
    boolean hasParking,
    @Schema(title = "쉴 수 있는 공간 여부")
    boolean hasRestArea,
    @Schema(title = "프라이빗 룸 여부")
    boolean hasPrivateRoom,
    @Schema(title = "조명 밝기 수준", example = "LIGHT")
    LightingLevel lightingLevel,
    @Schema(title = "혼잡도 수준", example = "HIGH")
    CrowdLevel crowdLevel,
    @Schema(title = "영업 시작 시간", example = "09:00")
    String businessStartTime,
    @Schema(title = "영업 종료 시간", example = "18:30")
    String businessClosingTime,
    @Schema(title = "휴무일", example = "매주 화요일")
    String dayOff
) {
    public static PlaceDetailResponse of(Place place) {
        return new PlaceDetailResponse(
            place.getId(),
            place.getName(),
            place.getDescription(),
            place.getCategory(),
            place.getAddress(),
            place.getLatitude(),
            place.getLongitude(),
            place.isWheelchairFriendly(),
            place.isQuiet(),
            place.isHasParking(),
            place.isHasRestArea(),
            place.isHasPrivateRoom(), 
            place.getLightingLevel(),
            place.getCrowdLevel(),
            place.getBusinessStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
            place.getBusinessClosingTime().format(DateTimeFormatter.ofPattern("HH:mm")),
            place.getDayOff()
        );
    }
}
