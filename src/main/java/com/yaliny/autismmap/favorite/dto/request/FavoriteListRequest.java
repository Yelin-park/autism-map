package com.yaliny.autismmap.favorite.dto.request;

import com.yaliny.autismmap.place.entity.LightingLevel;
import com.yaliny.autismmap.place.entity.PlaceCategory;

public record FavoriteListRequest(
    Long provinceId,
    Long districtId,
    PlaceCategory category,
    Boolean isQuiet,
    Boolean hasParking,
    Boolean hasRestArea,
    Boolean hasPrivateRoom,
    LightingLevel lightingLevel
) {

    public static FavoriteListRequest of(
        Long provinceId,
        Long districtId,
        PlaceCategory category,
        Boolean isQuiet,
        Boolean hasParking,
        Boolean hasRestArea,
        Boolean hasPrivateRoom,
        LightingLevel lightingLevel) {

        return new FavoriteListRequest(provinceId, districtId, category, isQuiet, hasParking, hasRestArea, hasPrivateRoom, lightingLevel);

    }
}
