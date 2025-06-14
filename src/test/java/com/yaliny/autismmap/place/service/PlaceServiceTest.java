package com.yaliny.autismmap.place.service;

import com.yaliny.autismmap.place.dto.request.PlaceCreateRequest;
import com.yaliny.autismmap.place.dto.request.PlaceListRequest;
import com.yaliny.autismmap.place.dto.request.PlaceUpdateRequest;
import com.yaliny.autismmap.place.dto.response.PlaceDetailResponse;
import com.yaliny.autismmap.place.dto.response.PlaceListResponse;
import com.yaliny.autismmap.place.entity.CrowdLevel;
import com.yaliny.autismmap.place.entity.LightingLevel;
import com.yaliny.autismmap.place.entity.Place;
import com.yaliny.autismmap.place.entity.PlaceCategory;
import com.yaliny.autismmap.place.repository.PlaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PlaceServiceTest {

    @Autowired
    private PlaceService placeService;

    @Autowired
    private PlaceRepository placeRepository;

    @BeforeEach
    void setUp() {
        placeRepository.deleteAll();
    }

    @Test
    @DisplayName("장소 등록 성공")
    void createPlace_success() {
        PlaceCreateRequest request = new PlaceCreateRequest(
            "테스트 장소",
            "설명입니다.",
            PlaceCategory.CAFE,
            "서울시",
            "강남구",
            "서울시 강남구",
            37.5665,
            126.9780,
            true,
            true,
            true,
            false,
            LightingLevel.MODERATE,
            CrowdLevel.NORMAL,
            "09:00",
            "19:00",
            "월요일"
        );

        Long placeId = placeService.createPlace(request);

        Place findPlace = placeRepository.findById(placeId).orElseThrow();
        assertThat(findPlace.getName()).isEqualTo(request.name());
        assertThat(findPlace.getLightingLevel()).isEqualTo(request.lightingLevel());
        assertThat(findPlace.getCategory()).isEqualTo(request.category());
        assertThat(findPlace.getBusinessStartTime()).isEqualTo(request.businessStartTime());
    }

    @Test
    @DisplayName("장소 수정 성공")
    void updatePlace_success() {
        Place savedPlace = placeRepository.save(new Place(
            "테스트 장소",
            "설명입니다.",
            PlaceCategory.CAFE,
            "서울시",
            "강남구",
            "서울시 강남구",
            37.5665,
            126.9780,
            true,
            true,
            true,
            false,
            LightingLevel.MODERATE,
            CrowdLevel.NORMAL,
            "09:00",
            "19:00",
            "월요일"
        ));

        PlaceUpdateRequest request = new PlaceUpdateRequest(
            "수정된 장소",
            "설명입니다.2",
            PlaceCategory.CAFE,
            "서울시",
            "강남구2",
            "서울시 강남구2",
            37.5665,
            126.9780,
            false,
            true,
            true,
            false,
            LightingLevel.MODERATE,
            CrowdLevel.NORMAL,
            "09:00",
            "18:00",
            "월요일"
        );

        PlaceDetailResponse response = placeService.updatePlace(savedPlace.getId(), request);

        Place findPlace = placeRepository.findById(response.id()).orElseThrow();
        assertThat(findPlace.getName()).isEqualTo(request.name());
        assertThat(findPlace.getDescription()).isEqualTo(request.description());
        assertThat(findPlace.getAddress()).isEqualTo(request.address());
        assertThat(findPlace.isQuiet()).isFalse();
        assertThat(findPlace.isHasParking()).isTrue();
        assertThat(findPlace.isHasRestArea()).isTrue();
        assertThat(findPlace.getLightingLevel()).isEqualTo(request.lightingLevel());
        assertThat(findPlace.getCategory()).isEqualTo(request.category());
        assertThat(findPlace.getBusinessStartTime()).isEqualTo(request.businessStartTime());
    }

    @Test
    @DisplayName("장소 삭제 성공")
    void deletePlace_success() {
        Place savedPlace = placeRepository.save(new Place(
            "테스트 장소",
            "설명입니다.",
            PlaceCategory.CAFE,
            "서울시",
            "강남구",
            "서울시 강남구",
            37.5665,
            126.9780,
            true,
            true,
            true,
            false,
            LightingLevel.MODERATE,
            CrowdLevel.NORMAL,
            "09:00",
            "19:00",
            "월요일"
        ));

        placeService.deletePlace(savedPlace.getId());

        assertThat(placeRepository.findById(savedPlace.getId())).isEmpty();
    }

    @Test
    @DisplayName("장소 목록 조회 성공")
    void getPlaceList_success() {
        placeRepository.save(new Place(
            "테스트 장소1",
            "설명입니다.",
            PlaceCategory.CAFE,
            "서울시",
            "강남구",
            "서울시 강남구",
            37.5665,
            126.9780,
            true,
            true,
            true,
            false,
            LightingLevel.MODERATE,
            CrowdLevel.NORMAL,
            "09:00",
            "19:00",
            "월요일"
        ));

        placeRepository.save(new Place(
            "테스트 장소2",
            "설명입니다.",
            PlaceCategory.CAFE,
            "서울시",
            "강남구",
            "서울시 강남구",
            37.5665,
            126.9780,
            true,
            true,
            true,
            false,
            LightingLevel.MODERATE,
            CrowdLevel.NORMAL,
            "09:00",
            "19:00",
            "월요일"
        ));

        placeRepository.save(new Place(
            "테스트 장소3",
            "설명입니다.",
            PlaceCategory.RESTAURANT,
            "서울시",
            "강남구",
            "서울시 강남구",
            37.5665,
            126.9780,
            false,
            false,
            false,
            false,
            LightingLevel.MODERATE,
            CrowdLevel.NORMAL,
            "09:00",
            "19:00",
            "월요일"
        ));

        PlaceListRequest request = new PlaceListRequest("서울시", null, PlaceCategory.RESTAURANT, null, null, null, null, null);

        PlaceListResponse result = placeService.getPlaceList(request, PageRequest.of(0, 10));

        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(result.content().size()).isEqualTo(1);
        assertThat(result.content().get(0).name()).isEqualTo("테스트 장소3");
        assertThat(result.content().get(0).isQuiet()).isFalse();
    }
}