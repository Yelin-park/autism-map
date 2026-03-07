package com.yaliny.autismmap.place.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaceExcelRowDto {

    private String category;
    private String name;

    private int noiseLevel;
    private int lightingLevel;
    private int crowdLevel;

    private boolean hasParking;
    private boolean hasRestArea;
    private boolean hasPrivateRoom;

    private String businessStartTime;
    private String businessClosingTime;

    private String dayOff;
    private String address;

    private Long provinceId;
    private Long districtId;

    private Double longitude;
    private Double latitude;

    private String description;
}
