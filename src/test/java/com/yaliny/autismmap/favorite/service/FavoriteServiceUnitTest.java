package com.yaliny.autismmap.favorite.service;

import com.yaliny.autismmap.favorite.entity.Favorite;
import com.yaliny.autismmap.favorite.repository.FavoriteRepository;
import com.yaliny.autismmap.global.exception.CustomException;
import com.yaliny.autismmap.member.entity.Member;
import com.yaliny.autismmap.member.repository.MemberRepository;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceUnitTest {

    @Mock
    MemberRepository memberRepository;
    @Mock
    PlaceRepository placeRepository;
    @Mock
    ProvinceRepository provinceRepository;
    @Mock
    FavoriteRepository favoriteRepository;

    @InjectMocks
    FavoriteService favoriteService;

    @Test
    @DisplayName("즐겨찾기 등록 성공")
    void addFavorite_success() {
        Long memberId = 1L, placeId = 10L;
        Member member = Member.createMember("a@a.com", "pw", "nick");
        District district1 = District.createDistrict("수원시");
        District district2 = District.createDistrict("안양시");
        Province province = provinceRepository.save(Province.createProvince("경기도", district1, district2));

        Place place = Place.createPlace("장소명", "설명", PlaceCategory.ATTRACTION,
            province, district2, "주소", 1.0, 2.0,
            false, true,
            true, true, LightingLevel.MODERATE,
            CrowdLevel.NORMAL, "09:00", "18:00", "월요일");

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(placeRepository.findById(placeId)).willReturn(Optional.of(place));
        given(favoriteRepository.existsByMemberIdAndPlaceId(memberId, placeId)).willReturn(false);

        favoriteService.addFavorite(memberId, placeId);

        verify(favoriteRepository, times(1)).save(any(Favorite.class));
    }

    @Test
    @DisplayName("등록 요청하는 즐겨찾기가 존재하면 save 호출 안 함")
    void addFavorite_skipWhenExists() {
        Long memberId = 1L, placeId = 10L;
        Member member = Member.createMember("a@a.com", "pw", "nick");
        District district1 = District.createDistrict("수원시");
        District district2 = District.createDistrict("안양시");
        Province province = provinceRepository.save(Province.createProvince("경기도", district1, district2));

        Place place = Place.createPlace("장소명", "설명", PlaceCategory.ATTRACTION,
            province, district2, "주소", 1.0, 2.0,
            false, true,
            true, true, LightingLevel.MODERATE,
            CrowdLevel.NORMAL, "09:00", "18:00", "월요일");

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(placeRepository.findById(placeId)).willReturn(Optional.of(place));
        given(favoriteRepository.existsByMemberIdAndPlaceId(memberId, placeId)).willReturn(true);

        favoriteService.addFavorite(memberId, placeId);

        verify(favoriteRepository, never()).save(any());
    }

    @Test
    @DisplayName("즐겨찾기 등록 실패 - 존재하지 않는 회원")
    void addFavorite_memberNotFound() {
        Long memberId = 99L, placeId = 10L;
        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> favoriteService.addFavorite(memberId, placeId))
            .isInstanceOf(CustomException.class);

        verify(placeRepository, never()).findById(anyLong());
        verify(favoriteRepository, never()).save(any());
    }

    @Test
    @DisplayName("즐겨찾기 등록 실패 - 존재하지 않는 장소")
    void addFavorite_placeNotFound() {
        Long memberId = 1L, placeId = 999L;
        given(memberRepository.findById(memberId)).willReturn(Optional.of(Member.createMember("a@a.com", "pw", "n")));
        given(placeRepository.findById(placeId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> favoriteService.addFavorite(memberId, placeId))
            .isInstanceOf(CustomException.class);

        verify(favoriteRepository, never()).save(any());
    }
}
