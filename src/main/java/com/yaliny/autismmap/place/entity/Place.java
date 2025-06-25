package com.yaliny.autismmap.place.entity;

import com.yaliny.autismmap.global.entity.BaseEntity;
import com.yaliny.autismmap.place.dto.request.PlaceUpdateRequest;
import com.yaliny.autismmap.region.entity.District;
import com.yaliny.autismmap.region.entity.Province;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Setter(AccessLevel.PRIVATE)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_id", nullable = false)
    private Province province; // 행정구역(도/특별시/광역시)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", nullable = false)
    private District district; // 시/군/구

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

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaceImage> images = new ArrayList<>();

    // 연관 관계 편의 메서드
    public void addImage(PlaceImage image) {
        this.images.add(image);
        image.setPlace(this);
    }

    // 생성 메서드
    public static Place createPlace(
        String name,
        String description,
        PlaceCategory category,
        Province province,
        District district,
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
        String dayOff,
        PlaceImage... images
    ) {
        Place place = new Place();
        place.setName(name);
        place.setDescription(description);
        place.setCategory(category);
        place.setProvince(province);
        place.setDistrict(district);
        place.setAddress(address);
        place.setLatitude(latitude);
        place.setLongitude(longitude);
        place.setQuiet(isQuiet);
        place.setHasParking(hasParking);
        place.setHasRestArea(hasRestArea);
        place.setHasPrivateRoom(hasPrivateRoom);
        place.setLightingLevel(lightingLevel);
        place.setCrowdLevel(crowdLevel);
        place.setBusinessStartTime(LocalTime.parse(businessStartTime));
        place.setBusinessClosingTime(LocalTime.parse(businessClosingTime));
        place.setDayOff(dayOff);
        for (PlaceImage image : images) {
            place.addImage(image);
        }
        return place;
    }

    public void updatePlace(PlaceUpdateRequest request, Province province, District district, List<PlaceImage> placeImages, List<PlaceImage> toPreserve) {
        this.name = request.name();
        this.description = request.description();
        this.category = request.category();
        this.province = province;
        this.district = district;
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
        this.images.clear();

        for (PlaceImage image : toPreserve) {
            this.addImage(image);
        }

        for (PlaceImage image : placeImages) {
            this.addImage(image);
        }
    }
}
