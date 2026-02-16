package com.yaliny.autismmap.favorite.dto.request;

import com.yaliny.autismmap.place.entity.LightingLevel;
import com.yaliny.autismmap.place.entity.NoiseLevel;
import com.yaliny.autismmap.place.entity.PlaceCategory;

public record FavoriteListRequest(
    Long provinceId,
    Long districtId,
    PlaceCategory category,
    NoiseLevel noiseLevel,
    Boolean hasParking,
    Boolean hasRestArea,
    Boolean hasPrivateRoom,
    LightingLevel lightingLevel
) {

    public static FavoriteListRequest of(
        Long provinceId,
        Long districtId,
        PlaceCategory category,
        NoiseLevel noiseLevel,
        Boolean hasParking,
        Boolean hasRestArea,
        Boolean hasPrivateRoom,
        LightingLevel lightingLevel) {

        return new FavoriteListRequest(provinceId, districtId, category, noiseLevel, hasParking, hasRestArea, hasPrivateRoom, lightingLevel);

    }
}
