package com.yaliny.autismmap.place.dto.request;

import com.yaliny.autismmap.place.entity.CrowdLevel;
import com.yaliny.autismmap.place.entity.LightingLevel;
import com.yaliny.autismmap.place.entity.PlaceCategory;
import io.swagger.v3.oas.annotations.media.Schema;

public interface PlaceCommonRequest {
    String name();
    String description();
    PlaceCategory category();
    Long provinceId();
    Long districtId();
    String address();
    Double latitude();
    Double longitude();
    boolean isQuiet();
    boolean hasParking();
    boolean hasRestArea();
    boolean hasPrivateRoom();
    LightingLevel lightingLevel();
    CrowdLevel crowdLevel();
    String businessStartTime();
    String businessClosingTime();
    String dayOff();
}
