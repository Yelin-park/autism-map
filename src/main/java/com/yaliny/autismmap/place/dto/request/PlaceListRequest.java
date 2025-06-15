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
}
