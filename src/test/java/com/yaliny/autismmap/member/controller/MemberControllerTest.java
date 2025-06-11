package com.yaliny.autismmap.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaliny.autismmap.global.jwt.JwtUtil;
import com.yaliny.autismmap.member.dto.LoginRequest;
import com.yaliny.autismmap.member.entity.Member;
import com.yaliny.autismmap.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
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
        // 테스트용 사용자 저장
        Member member = new Member("test@example.com", passwordEncoder.encode("1234"), "테스터");
        memberRepository.save(member);
    }

    @Test
    @DisplayName("/api/v1/member/login → JWT 발급 성공 테스트")
    void login_정상_요청_테스트() throws Exception {
        // given
        LoginRequest request = new LoginRequest("test@example.com", "1234");

        // when & then
        mockMvc.perform(post("/api/v1/member/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value("OK"))
            .andExpect(jsonPath("$.data.token").exists()); // token 필드 존재 여부 확인
    }

}