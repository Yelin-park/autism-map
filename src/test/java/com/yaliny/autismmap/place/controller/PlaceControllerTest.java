package com.yaliny.autismmap.place.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaliny.autismmap.global.jwt.JwtUtil;
import com.yaliny.autismmap.member.entity.Member;
import com.yaliny.autismmap.member.entity.Role;
import com.yaliny.autismmap.member.repository.MemberRepository;
import com.yaliny.autismmap.place.dto.request.PlaceCreateRequest;
import com.yaliny.autismmap.place.dto.request.PlaceUpdateRequest;
import com.yaliny.autismmap.place.entity.CrowdLevel;
import com.yaliny.autismmap.place.entity.LightingLevel;
import com.yaliny.autismmap.place.entity.Place;
import com.yaliny.autismmap.place.entity.PlaceCategory;
import com.yaliny.autismmap.place.repository.PlaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.parameters.P;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private String token;

    @BeforeEach
    void setUp() {
        placeRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("장소 등록 성공 테스트")
    void createPlace_success() throws Exception {
        Member member = memberRepository.save(new Member("admin@example.com", "1234", "관리자", Role.ADMIN));
        token = jwtUtil.generateToken(member.getId(), member.getEmail(), String.valueOf(member.getRole()));

        PlaceCreateRequest request = new PlaceCreateRequest(
            "테스트 장소",
            "설명입니다.",
            PlaceCategory.CAFE,
            "서울시 강남구",
            37.5665,
            126.9780,
            true,
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
            "서울시 강남구",
            37.5665,
            126.9780,
            true,
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

        Place savedPlace = placeRepository.save(new Place(
            "테스트 장소",
            "설명입니다.",
            PlaceCategory.CAFE,
            "서울시 강남구",
            37.5665,
            126.9780,
            true,
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
            "서울시 강남구2",
            37.5665,
            126.9780,
            false,
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
        assertThat(findPlace.getAddress()).isEqualTo("서울시 강남구2");
        assertThat(findPlace.getDayOff()).isEqualTo("월요일");
    }

    @Test
    @DisplayName("장소 수정 실패 - 관리자 권한 없음")
    void updatePlace_fail() throws Exception {
        Member member = memberRepository.save(new Member("test@example.com", "1234", "사용자", Role.USER));
        token = jwtUtil.generateToken(member.getId(), member.getEmail(), String.valueOf(member.getRole()));

        Place savedPlace = placeRepository.save(new Place(
            "테스트 장소",
            "설명입니다.",
            PlaceCategory.CAFE,
            "서울시 강남구",
            37.5665,
            126.9780,
            true,
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
            "서울시 강남구2",
            37.5665,
            126.9780,
            false,
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

        Place savedPlace = placeRepository.save(new Place(
            "테스트 장소",
            "설명입니다.",
            PlaceCategory.CAFE,
            "서울시 강남구",
            37.5665,
            126.9780,
            true,
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
            "서울시 강남구2",
            37.5665,
            126.9780,
            false,
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
}
