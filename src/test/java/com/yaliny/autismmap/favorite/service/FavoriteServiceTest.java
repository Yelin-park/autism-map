package com.yaliny.autismmap.favorite.service;

import com.yaliny.autismmap.favorite.entity.Favorite;
import com.yaliny.autismmap.favorite.repository.FavoriteRepository;
import com.yaliny.autismmap.global.security.CustomUserDetails;
import com.yaliny.autismmap.member.entity.Member;
import com.yaliny.autismmap.member.repository.MemberRepository;
import com.yaliny.autismmap.place.entity.CrowdLevel;
import com.yaliny.autismmap.place.entity.LightingLevel;
import com.yaliny.autismmap.place.entity.Place;
import com.yaliny.autismmap.place.entity.PlaceCategory;
import com.yaliny.autismmap.place.repository.PlaceRepository;
import com.yaliny.autismmap.region.entity.District;
import com.yaliny.autismmap.region.entity.Province;
import com.yaliny.autismmap.region.repository.DistrictRepository;
import com.yaliny.autismmap.region.repository.ProvinceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class FavoriteServiceTest {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private ProvinceRepository provinceRepository;

    @Autowired
    private DistrictRepository districtRepository;

    @BeforeEach
    void setUp() {
        favoriteRepository.deleteAll();
        memberRepository.deleteAll();
        placeRepository.deleteAll();
        districtRepository.deleteAll();
        provinceRepository.deleteAll();
    }

    private Member getMember(String postEmail, String postNickname) {
        Member member = Member.createMember(postEmail, "test1234", postNickname);
        return memberRepository.save(member);
    }

    private Province getProvince(District... districts) {
        return provinceRepository.save(Province.createProvince("강원도", districts));
    }

    private Place getPlace() {
        District district1 = District.createDistrict("속초시");
        District district2 = District.createDistrict("횡성시");
        Province province = getProvince(district1, district2);

        Place savedPlace = placeRepository.save(Place.createPlace(
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

        return savedPlace;
    }

    private void setAuthentication(Member member) {
        CustomUserDetails userDetails = new CustomUserDetails(
            member.getId(),
            member.getEmail(),
            member.getRole().name(),
            List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()))
        );
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("즐겨찾기 등록 성공")
    void addFavorite_success() {
        Member member = getMember("test1234@test.com", "테스터");
        District district1 = District.createDistrict("속초시");
        District district2 = District.createDistrict("횡성시");
        Province province = getProvince(district1, district2);

        Place place = placeRepository.save(Place.createPlace(
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
        setAuthentication(member);

        favoriteService.addFavorite(member.getId(), place.getId());

        Favorite findFavorite = favoriteRepository.findByMemberIdAndPlaceId(member.getId(), place.getId()).get();
        assertThat(findFavorite).isNotNull();
        assertThat(findFavorite.getMember().getNickname()).isEqualTo(member.getNickname());
        clearAuthentication();
    }

    @Test
    @DisplayName("즐겨찾기 삭제 성공")
    void deleteFavorite_success() {
        Member member = getMember("test1234@test.com", "테스터");
        District district1 = District.createDistrict("속초시");
        District district2 = District.createDistrict("횡성시");
        Province province = getProvince(district1, district2);

        Place place = placeRepository.save(Place.createPlace(
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
        setAuthentication(member);
        Favorite favorite = favoriteRepository.save(Favorite.createFavorite(member, place));

        favoriteService.deleteFavorite(member.getId(), favorite.getId());
        Favorite findFavorite = favoriteRepository.findById(favorite.getId()).orElse(null);
        assertThat(findFavorite).isNull();
        clearAuthentication();
    }

}