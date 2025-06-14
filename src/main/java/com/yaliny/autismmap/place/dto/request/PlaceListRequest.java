package com.yaliny.autismmap.place.dto.request;

import com.yaliny.autismmap.place.entity.LightingLevel;
import com.yaliny.autismmap.place.entity.PlaceCategory;

public record PlaceListRequest(
    String region,
    String city,
    PlaceCategory category,
    Boolean isQuiet,
    Boolean hasParking,
    Boolean hasRestArea,
    Boolean hasPrivateRoom,
    LightingLevel lightingLevel
) {
}
