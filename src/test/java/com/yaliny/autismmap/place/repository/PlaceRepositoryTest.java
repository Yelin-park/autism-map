package com.yaliny.autismmap.place.repository;

import com.yaliny.autismmap.place.entity.CrowdLevel;
import com.yaliny.autismmap.place.entity.LightingLevel;
import com.yaliny.autismmap.place.entity.Place;
import com.yaliny.autismmap.place.entity.PlaceCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PlaceRepositoryTest {

    @Autowired
    private PlaceRepository placeRepository;

    @DisplayName("H2 DB 연결 테스트 - Place 저장 및 조회")
    @Test
    void saveAndFindPlace() {
        Place place = new Place("장소명", "설명", PlaceCategory.ATTRACTION,
            "주소", 1.0, 2.0,
            false, true, true,
            true, true, LightingLevel.MODERATE,
            CrowdLevel.NORMAL, "09:00", "18:00", "월요일");

        Place savedPlace = placeRepository.save(place);
        Place findPlace = placeRepository.findById(savedPlace.getId()).orElseThrow();

        assertThat(findPlace.getName()).isEqualTo(place.getName());
        assertThat(findPlace.getLightingLevel()).isEqualTo(place.getLightingLevel());
    }

}