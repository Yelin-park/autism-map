package com.yaliny.autismmap.place.dto.request;

import com.yaliny.autismmap.place.entity.LightingLevel;
import com.yaliny.autismmap.place.entity.PlaceCategory;

public record PlaceListRequest(
    Long provinceId,
    Long districtId,
    PlaceCategory category,
    Boolean isQuiet,
    Boolean hasParking,
    Boolean hasRestArea,
    Boolean hasPrivateRoom,
    LightingLevel lightingLevel
) {

    public static PlaceListRequest of(
        Long provinceId,
        Long districtId,
        PlaceCategory category,
        Boolean isQuiet,
        Boolean hasParking,
        Boolean hasRestArea,
        Boolean hasPrivateRoom,
        LightingLevel lightingLevel) {

        return new PlaceListRequest(provinceId, districtId, category, isQuiet, hasParking, hasRestArea, hasPrivateRoom, lightingLevel);

    }
}
