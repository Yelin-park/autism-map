package com.yaliny.autismmap.member.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaliny.autismmap.global.jwt.JwtUtil;
import com.yaliny.autismmap.global.response.BaseResponse;
import com.yaliny.autismmap.member.dto.request.LoginRequest;
import com.yaliny.autismmap.member.dto.response.LoginResponse;
import com.yaliny.autismmap.member.dto.request.SignUpRequest;
import com.yaliny.autismmap.member.entity.Member;
import com.yaliny.autismmap.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("JWT 발급 성공 테스트")
    void login_success() throws Exception {
        // given
        // 테스트용 사용자 저장
        Member member = Member.createMember("test@example.com", passwordEncoder.encode("1234"), "테스터");
        memberRepository.save(member);

        LoginRequest request = new LoginRequest("test@example.com", "1234");

        // when & then
        mockMvc.perform(post("/api/v1/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value("OK"))
            .andExpect(jsonPath("$.data.token").exists()); // token 필드 존재 여부 확인
    }

    @Test
    @DisplayName("이메일 중복 시 MemberAlreadyExistsException 발생 → 409 CONFLICT 응답")
    void signup_email_duplicate() throws Exception {
        SignUpRequest request = new SignUpRequest("test@test.com", "test1234@", "테스터");

        mockMvc.perform(post("/api/v1/members/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/api/v1/members/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict()) // 409 CONFLICT 응답 기대
            .andExpect(jsonPath("$.code").value(409))
            .andExpect(jsonPath("$.message").value("이미 존재하는 이메일입니다.")); // 정확한 에러 메시지 검증
    }

    @Test
    @DisplayName("로그아웃 성공 테스트")
    @Disabled
    void logout_success() throws Exception {
        // 회원가입 요청
        SignUpRequest request = new SignUpRequest("test1@example.com", "1234", "테스터");

        mockMvc.perform(post("/api/v1/members/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        // 로그인 후 JWT 발급 받기
        LoginRequest loginRequest = new LoginRequest("test1@example.com", "1234");

        String responseBody = mockMvc.perform(post("/api/v1/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        // BaseResponse<LoginResponse>로 파싱
        BaseResponse<LoginResponse> baseResponse = objectMapper.readValue(
            responseBody,
            new TypeReference<>() {
            }
        );

        token = baseResponse.data().token();

        mockMvc.perform(post("/api/v1/members/logout")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").value("로그아웃 성공"));
    }

    @Test
    @DisplayName("회원탈퇴 성공 테스트")
    void withdraw_success() throws Exception {
        // 회원가입 요청
        SignUpRequest request = new SignUpRequest("test2@example.com", "test1234@", "테스터");

        mockMvc.perform(post("/api/v1/members/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        // 로그인 후 JWT 발급 받기
        LoginRequest loginRequest = new LoginRequest("test2@example.com", "test1234@");

        String responseBody = mockMvc.perform(post("/api/v1/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        // BaseResponse<LoginResponse>로 파싱
        BaseResponse<LoginResponse> baseResponse = objectMapper.readValue(
            responseBody,
            new TypeReference<>() {
            }
        );

        token = baseResponse.data().token();

        mockMvc.perform(delete("/api/v1/members/{memberId}", jwtUtil.getMemberId(token))
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").value("회원탈퇴 성공"));

        // 회원탈퇴 후 DB에 없는지 검증
        assertThat(memberRepository.findByEmail("test2@example.com")).isEmpty();
    }

    @Test
    @DisplayName("회원 정보 조회 성공 테스트")
    void getMemberInfo_success() throws Exception {
        // 회원가입 요청
        SignUpRequest request = new SignUpRequest("test3@example.com", "test1234@", "테스터3");

        mockMvc.perform(post("/api/v1/members/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        // 로그인 후 JWT 발급 받기
        LoginRequest loginRequest = new LoginRequest("test3@example.com", "test1234@");

        String responseBody = mockMvc.perform(post("/api/v1/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        // BaseResponse<LoginResponse>로 파싱
        BaseResponse<LoginResponse> baseResponse = objectMapper.readValue(
            responseBody,
            new TypeReference<>() {
            }
        );

        token = baseResponse.data().token();

        mockMvc.perform(get("/api/v1/members/{memberId}", jwtUtil.getMemberId(token))
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.email").value("test3@example.com"))
            .andExpect(jsonPath("$.data.nickname").value("테스터3"));
    }

    @Test
    @DisplayName("닉네임 수정 성공 테스트")
    void updateNickname_success() throws Exception {
        // 회원가입 요청
        SignUpRequest request = new SignUpRequest("test2@example.com", "test1234@", "테스터");

        mockMvc.perform(post("/api/v1/members/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        // 로그인 후 JWT 발급 받기
        LoginRequest loginRequest = new LoginRequest("test2@example.com", "test1234@");

        String responseBody = mockMvc.perform(post("/api/v1/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        // BaseResponse<LoginResponse>로 파싱
        BaseResponse<LoginResponse> baseResponse = objectMapper.readValue(
            responseBody,
            new TypeReference<>() {
            }
        );

        token = baseResponse.data().token();
        String nickName = "수정닉네임";
        mockMvc.perform(patch("/api/v1/members/{memberId}/nickname", jwtUtil.getMemberId(token))
                .header("Authorization", "Bearer " + token)
                .param("nickname", nickName))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // 닉네임 수정 확인
        assertThat(memberRepository.findByEmail("test2@example.com").get().getNickname()).isEqualTo("수정닉네임");
    }
}