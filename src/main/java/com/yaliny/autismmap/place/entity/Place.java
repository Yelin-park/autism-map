package com.yaliny.autismmap.place.entity;

import com.yaliny.autismmap.global.entity.BaseEntity;
import com.yaliny.autismmap.place.dto.request.PlaceUpdateRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Place extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 장소명

    @Lob
    private String description; // 설명

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlaceCategory category; // 카테고리

    @Column(nullable = false)
    private String region; // 행정구역(도/특별시/광역시)

    @Column(nullable = false)
    private String city; // 시/군/구

    @Column(nullable = false)
    private String address; // 주소

    @Column(nullable = false)
    private Double latitude; // 위도

    @Column(nullable = false)
    private Double longitude; // 경도

    @Column(nullable = false)
    private boolean isQuiet; // 조용한 환경 여부

    @Column(nullable = false)
    private boolean hasParking; // 주차장 유무

    @Column(nullable = false)
    private boolean hasRestArea; // 쉴 수 있는 공간 여부

    @Column(nullable = false)
    private boolean hasPrivateRoom; // 프라이빗 룸 여부

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LightingLevel lightingLevel; // 조명 수준

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CrowdLevel crowdLevel; // 혼잡도

    @Column(nullable = false)
    private LocalTime businessStartTime; // 영업 시작 시간

    @Column(nullable = false)
    private LocalTime businessClosingTime; // 영업 종료 시간

    @Column(nullable = false)
    private String dayOff; // 휴무일

    public Place(
        String name,
        String description,
        PlaceCategory category,
        String region,
        String city,
        String address,
        Double latitude,
        Double longitude,
        boolean isQuiet,
        boolean hasParking,
        boolean hasRestArea,
        boolean hasPrivateRoom,
        LightingLevel lightingLevel,
        CrowdLevel crowdLevel,
        String businessStartTime,
        String businessClosingTime,
        String dayOff
    ) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.region = region;
        this.city = city;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isQuiet = isQuiet;
        this.hasParking = hasParking;
        this.hasRestArea = hasRestArea;
        this.hasPrivateRoom = hasPrivateRoom;
        this.lightingLevel = lightingLevel;
        this.crowdLevel = crowdLevel;
        this.businessStartTime = LocalTime.parse(businessStartTime);
        this.businessClosingTime = LocalTime.parse(businessClosingTime);
        this.dayOff = dayOff;
    }

    public void updatePlace(PlaceUpdateRequest request) {
        this.name = request.name();
        this.description = request.description();
        this.category = request.category();
        this.region = request.region();
        this.city = request.city();
        this.address = request.address();
        this.latitude = request.latitude();
        this.longitude = request.longitude();
        this.isQuiet = request.isQuiet();
        this.hasParking = request.hasParking();
        this.hasRestArea = request.hasRestArea();
        this.hasPrivateRoom = request.hasPrivateRoom();
        this.lightingLevel = request.lightingLevel();
        this.crowdLevel = request.crowdLevel();
        this.businessStartTime = LocalTime.parse(request.businessStartTime());
        this.businessClosingTime = LocalTime.parse(request.businessClosingTime());
        this.dayOff = request.dayOff();
    }
}
