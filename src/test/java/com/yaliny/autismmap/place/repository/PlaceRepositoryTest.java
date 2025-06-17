package com.yaliny.autismmap.place.repository;

import com.yaliny.autismmap.place.entity.CrowdLevel;
import com.yaliny.autismmap.place.entity.LightingLevel;
import com.yaliny.autismmap.place.entity.Place;
import com.yaliny.autismmap.place.entity.PlaceCategory;
import com.yaliny.autismmap.region.entity.District;
import com.yaliny.autismmap.region.entity.Province;
import com.yaliny.autismmap.region.repository.ProvinceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
class PlaceRepositoryTest {

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private ProvinceRepository provinceRepository;

    @DisplayName("H2 DB 연결 테스트 - Place 저장 및 조회")
    @Test
    void saveAndFindPlace() {
        District district1 = District.createDistrict("수원시");
        District district2 = District.createDistrict("안양시");
        Province province = provinceRepository.save(Province.createProvince("경기도", district1, district2));

        Place place = Place.createPlace("장소명", "설명", PlaceCategory.ATTRACTION,
            province,district2, "주소", 1.0, 2.0,
            false, true,
            true, true, LightingLevel.MODERATE,
            CrowdLevel.NORMAL, "09:00", "18:00", "월요일");

        Place savedPlace = placeRepository.save(place);
        Place findPlace = placeRepository.findById(savedPlace.getId()).orElseThrow();

        assertThat(findPlace.getName()).isEqualTo(place.getName());
        assertThat(findPlace.getLightingLevel()).isEqualTo(place.getLightingLevel());
    }

}