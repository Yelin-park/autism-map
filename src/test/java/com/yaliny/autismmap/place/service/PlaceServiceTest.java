package com.yaliny.autismmap.place.service;

import com.yaliny.autismmap.global.exception.PlaceNotFoundException;
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
import com.yaliny.autismmap.region.entity.District;
import com.yaliny.autismmap.region.entity.Province;
import com.yaliny.autismmap.region.repository.ProvinceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class PlaceServiceTest {

    @Autowired
    private PlaceService placeService;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private ProvinceRepository provinceRepository;

    @BeforeEach
    void setUp() {
        placeRepository.deleteAll();
        provinceRepository.deleteAll();
    }

    @Test
    @DisplayName("장소 등록 성공")
    void createPlace_success() {
        District district1 = District.createDistrict("수원시");
        District district2 = District.createDistrict("안양시");
        Province province = provinceRepository.save(Province.createProvince("경기도", district1, district2));

        PlaceCreateRequest request = new PlaceCreateRequest(
            "테스트 장소",
            "설명입니다.",
            PlaceCategory.CAFE,
            province.getId(),
            district2.getId(),
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
        District district1 = District.createDistrict("수원시");
        District district2 = District.createDistrict("안양시");
        Province province = provinceRepository.save(Province.createProvince("경기도", district1, district2));

        Place savedPlace = placeRepository.save(new Place(
            "테스트 장소",
            "설명입니다.",
            PlaceCategory.CAFE,
            province,
            district1,
            "경기도 수원시",
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
            province.getId(),
            district2.getId() ,
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
        District district1 = District.createDistrict("수원시");
        District district2 = District.createDistrict("안양시");
        Province province = provinceRepository.save(Province.createProvince("경기도", district1, district2));

        Place savedPlace = placeRepository.save(new Place(
            "테스트 장소",
            "설명입니다.",
            PlaceCategory.CAFE,
            province,
            district1,
            "경기도 수원시",
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
    @DisplayName("장소 삭제 실패 - 존재하지 않는 장소")
    void deletePlace_not_found() {
        District district1 = District.createDistrict("수원시");
        District district2 = District.createDistrict("안양시");
        Province province = provinceRepository.save(Province.createProvince("경기도", district1, district2));

        Place savedPlace = placeRepository.save(new Place(
            "테스트 장소",
            "설명입니다.",
            PlaceCategory.CAFE,
            province,
            district1,
            "경기도 수원시",
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

        assertThatThrownBy(() -> placeService.deletePlace(savedPlace.getId() + 1))
            .isInstanceOf(PlaceNotFoundException.class)
            .hasMessage("장소가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("장소 목록 조회 성공")
    void getPlaceList_success() {
        District district1 = District.createDistrict("수원시");
        District district2 = District.createDistrict("안양시");
        Province province = provinceRepository.save(Province.createProvince("경기도", district1, district2));

        placeRepository.save(new Place(
            "테스트 장소1",
            "설명입니다.",
            PlaceCategory.CAFE,
            province,
            district1,
            "경기도 수원시",
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
            province,
            district1,
            "경기도 수원시",
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
            PlaceCategory.CAFE,
            province,
            district1,
            "경기도 수원시",
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
            "테스트 장소4",
            "설명입니다.",
            PlaceCategory.RESTAURANT,
            province,
            district2,
            "경기도 안양시",
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

        PlaceListRequest request = new PlaceListRequest(null, district2.getId(), PlaceCategory.RESTAURANT, null, null, null, null, null);

        PlaceListResponse result = placeService.getPlaceList(request, PageRequest.of(0, 10));

        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(result.content().size()).isEqualTo(1);
        assertThat(result.content().get(0).name()).isEqualTo("테스트 장소4");
    }

    @Test
    @DisplayName("장소 상세 조회 성공")
    void getPlaceDetail_success() {
        District district1 = District.createDistrict("수원시");
        District district2 = District.createDistrict("안양시");
        Province province = provinceRepository.save(Province.createProvince("경기도", district1, district2));

        Place savedPlace = placeRepository.save(new Place(
            "테스트 장소1",
            "설명입니다.",
            PlaceCategory.CAFE,
            province,
            district1,
            "경기도 수원시",
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

        PlaceDetailResponse result = placeService.getPlaceDetail(savedPlace.getId());

        assertThat(result.id()).isEqualTo(savedPlace.getId());
        assertThat(result.name()).isEqualTo(savedPlace.getName());
        assertThat(result.category()).isEqualTo(savedPlace.getCategory().getDescription());
        assertThat(result.lightingLevel()).isEqualTo(savedPlace.getLightingLevel().getDescription());
        assertThat(result.address()).isEqualTo(savedPlace.getAddress());
    }

    @Test
    @DisplayName("장소 상세 조회 실패 - 존재하지 않는 장소")
    void getPlaceDetail_not_found() {
        District district1 = District.createDistrict("수원시");
        District district2 = District.createDistrict("안양시");
        Province province = provinceRepository.save(Province.createProvince("경기도", district1, district2));

        Place savedPlace = placeRepository.save(new Place(
            "테스트 장소1",
            "설명입니다.",
            PlaceCategory.CAFE,
            province,
            district1,
            "경기도 수원시",
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

        assertThatThrownBy(() -> placeService.getPlaceDetail(savedPlace.getId() + 1))
            .isInstanceOf(PlaceNotFoundException.class)
            .hasMessage("장소가 존재하지 않습니다.");
    }
}