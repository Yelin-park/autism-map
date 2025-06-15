package com.yaliny.autismmap.place.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaliny.autismmap.global.jwt.JwtUtil;
import com.yaliny.autismmap.member.entity.Member;
import com.yaliny.autismmap.member.entity.Role;
import com.yaliny.autismmap.member.repository.MemberRepository;
import com.yaliny.autismmap.place.dto.request.PlaceCreateRequest;
import com.yaliny.autismmap.place.dto.request.PlaceListRequest;
import com.yaliny.autismmap.place.dto.request.PlaceUpdateRequest;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PlaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProvinceRepository provinceRepository;

    @Autowired
    private DistrictRepository districtRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private String token;

    @BeforeEach
    void setUp() {
        placeRepository.deleteAll();
        memberRepository.deleteAll();
        provinceRepository.deleteAll();
    }

    @Test
    @DisplayName("장소 등록 성공 테스트")
    void createPlace_success() throws Exception {
        Member member = memberRepository.save(new Member("admin@example.com", "1234", "관리자", Role.ADMIN));
        token = jwtUtil.generateToken(member.getId(), member.getEmail(), String.valueOf(member.getRole()));

        District district1 = District.createDistrict("수원시");
        District district2 = District.createDistrict("안양시");
        Province province = provinceRepository.save(Province.createProvince("경기도", district1, district2));

        PlaceCreateRequest request = new PlaceCreateRequest(
            "테스트 장소",
            "설명입니다.",
            PlaceCategory.CAFE,
            province.getId(),
            district1.getId(),
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
        );

        mockMvc.perform(post("/api/v1/places")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").exists());

        assertThat(placeRepository.findAll()).hasSize(1);
        assertThat(placeRepository.findAll().get(0).getName()).isEqualTo("테스트 장소");
    }

    @Test
    @DisplayName("장소 등록 실패 - 관리자 권한 없음")
    void createPlace_fail() throws Exception {
        Member member = memberRepository.save(new Member("test@example.com", "1234", "사용자", Role.USER));
        token = jwtUtil.generateToken(member.getId(), member.getEmail(), String.valueOf(member.getRole()));

        PlaceCreateRequest request = new PlaceCreateRequest(
            "테스트 장소",
            "설명입니다.",
            PlaceCategory.CAFE,
            1L,
            1L,
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

        mockMvc.perform(post("/api/v1/places")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(403))
            .andExpect(jsonPath("$.message").value("권한이 없습니다."));
    }

    @Test
    @DisplayName("장소 수정 성공 테스트")
    void updatePlace_success() throws Exception {
        Member member = memberRepository.save(new Member("admin@example.com", "1234", "관리자", Role.ADMIN));
        token = jwtUtil.generateToken(member.getId(), member.getEmail(), String.valueOf(member.getRole()));

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
            district2.getId(),
            "경기도 안양시",
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

        mockMvc.perform(patch("/api/v1/places/{placeId}", savedPlace.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").exists());

        Place findPlace = placeRepository.findById(savedPlace.getId()).get();

        assertThat(findPlace.getName()).isEqualTo("수정된 장소");
        assertThat(findPlace.isHasParking()).isTrue();
        assertThat(findPlace.isQuiet()).isFalse();
        assertThat(findPlace.getDescription()).isEqualTo("설명입니다.2");
        assertThat(findPlace.getAddress()).isEqualTo("경기도 안양시");
        assertThat(findPlace.getDayOff()).isEqualTo("월요일");
    }

    @Test
    @DisplayName("장소 수정 실패 - 관리자 권한 없음")
    void updatePlace_fail() throws Exception {
        Member member = memberRepository.save(new Member("test@example.com", "1234", "사용자", Role.USER));
        token = jwtUtil.generateToken(member.getId(), member.getEmail(), String.valueOf(member.getRole()));

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
            district2.getId(),
            "경기도 안양시",
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

        mockMvc.perform(patch("/api/v1/places/{placeId}", savedPlace.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(403))
            .andExpect(jsonPath("$.message").value("권한이 없습니다."));
    }

    @Test
    @DisplayName("장소 수정 실패 - 존재하지 않는 장소")
    void updatePlace_not_found() throws Exception {
        Member member = memberRepository.save(new Member("admin@example.com", "1234", "관리자", Role.ADMIN));
        token = jwtUtil.generateToken(member.getId(), member.getEmail(), String.valueOf(member.getRole()));

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
            district2.getId(),
            "경기도 안양시",
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

        mockMvc.perform(patch("/api/v1/places/{placeId}", savedPlace.getId() + 1)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(404))
            .andExpect(jsonPath("$.message").value("장소가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("장소 삭제 성공 테스트")
    void deletePlace_success() throws Exception {
        Member member = memberRepository.save(new Member("admin@example.com", "1234", "관리자", Role.ADMIN));
        token = jwtUtil.generateToken(member.getId(), member.getEmail(), String.valueOf(member.getRole()));

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

        mockMvc.perform(delete("/api/v1/places/{placeId}", savedPlace.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").exists());

        assertThat(placeRepository.findById(savedPlace.getId())).isEmpty();
    }

    @Test
    @DisplayName("장소 삭제 실패 - 관리자 권한 없음")
    void deletePlace_fail() throws Exception {
        Member member = memberRepository.save(new Member("test@example.com", "1234", "사용자", Role.USER));
        token = jwtUtil.generateToken(member.getId(), member.getEmail(), String.valueOf(member.getRole()));

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

        mockMvc.perform(delete("/api/v1/places/{placeId}", savedPlace.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(403))
            .andExpect(jsonPath("$.message").value("권한이 없습니다."));
    }

    @Test
    @DisplayName("장소 삭제 실패 - 존재하지 않는 장소")
    void deletePlace_not_found() throws Exception {
        Member member = memberRepository.save(new Member("admin@example.com", "1234", "관리자", Role.ADMIN));
        token = jwtUtil.generateToken(member.getId(), member.getEmail(), String.valueOf(member.getRole()));

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

        mockMvc.perform(delete("/api/v1/places/{placeId}", savedPlace.getId() + 1)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(404))
            .andExpect(jsonPath("$.message").value("장소가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("장소 목록 조회 성공")
    void getPlaceList_success() throws Exception {
        Member member = memberRepository.save(new Member("admin@example.com", "1234", "관리자", Role.ADMIN));
        token = jwtUtil.generateToken(member.getId(), member.getEmail(), String.valueOf(member.getRole()));

        District district1 = District.createDistrict("수원시");
        District district2 = District.createDistrict("안양시");
        Province province = provinceRepository.save(Province.createProvince("경기도", district1, district2));

        placeRepository.save(new Place(
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

        placeRepository.save(new Place(
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

        placeRepository.save(new Place(
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

        placeRepository.save(new Place(
            "테스트 장소",
            "설명입니다.",
            PlaceCategory.CAFE,
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

        PlaceListRequest request = new PlaceListRequest(province.getId(), null, PlaceCategory.CAFE, null, null, null, null, null);

        mockMvc.perform(get("/api/v1/places")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .param("page", "0")
                .param("size", "10")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.totalElements").value("4"));
    }

    @Test
    @DisplayName("장소 상세 조회 성공")
    void getPlaceDetail_success() throws Exception {
        Member member = memberRepository.save(new Member("test@example.com", "1234", "사용자", Role.USER));
        token = jwtUtil.generateToken(member.getId(), member.getEmail(), String.valueOf(member.getRole()));

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

        mockMvc.perform(get("/api/v1/places/{placeId}", savedPlace.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("장소 상세 조회 실패 - 존재하지 않는 장소")
    void getPlaceDetail_not_found() throws Exception {
        Member member = memberRepository.save(new Member("test@example.com", "1234", "사용자", Role.USER));
        token = jwtUtil.generateToken(member.getId(), member.getEmail(), String.valueOf(member.getRole()));

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

        mockMvc.perform(get("/api/v1/places/{placeId}", savedPlace.getId() + 1)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(404))
            .andExpect(jsonPath("$.message").value("장소가 존재하지 않습니다."));
    }
}
