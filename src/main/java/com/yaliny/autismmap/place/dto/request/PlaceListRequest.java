package com.yaliny.autismmap.place.dto.request;

import com.yaliny.autismmap.place.entity.LightingLevel;
import com.yaliny.autismmap.place.entity.NoiseLevel;
import com.yaliny.autismmap.place.entity.PlaceCategory;

public record PlaceListRequest(
    Long provinceId,
    Long districtId,
    PlaceCategory category,
    NoiseLevel noiseLevel,
    Boolean hasParking,
    Boolean hasRestArea,
    Boolean hasPrivateRoom,
    LightingLevel lightingLevel
) {

    public static PlaceListRequest of(
        Long provinceId,
        Long districtId,
        PlaceCategory category,
        NoiseLevel noiseLevel,
        Boolean hasParking,
        Boolean hasRestArea,
        Boolean hasPrivateRoom,
        LightingLevel lightingLevel) {

        return new PlaceListRequest(provinceId, districtId, category, noiseLevel, hasParking, hasRestArea, hasPrivateRoom, lightingLevel);

    }
}
