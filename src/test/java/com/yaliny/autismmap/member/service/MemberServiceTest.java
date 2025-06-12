package com.yaliny.autismmap.member.service;

import com.yaliny.autismmap.global.exception.InvalidPasswordException;
import com.yaliny.autismmap.global.exception.MemberAlreadyExistsException;
import com.yaliny.autismmap.global.exception.MemberNotFoundException;
import com.yaliny.autismmap.global.exception.NoPermissionException;
import com.yaliny.autismmap.global.jwt.JwtUtil;
import com.yaliny.autismmap.member.dto.*;
import com.yaliny.autismmap.member.entity.Member;
import com.yaliny.autismmap.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입 성공")
    void signup_success() {
        SignUpRequest request = new SignUpRequest("test@test.com", "1234", "테스터");

        SignUpResponse response = memberService.signup(request);

        assertThat(response.token()).isNotBlank();

        Member savedMember = memberRepository.findByEmail("test@test.com").orElseThrow();
        assertThat(savedMember.getNickname()).isEqualTo("테스터");
        assertThat(passwordEncoder.matches("1234", savedMember.getPassword())).isTrue();
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복 예외 발생")
    void signup_duplicate_email() {
        SignUpRequest request = new SignUpRequest("test@test.com", "1234", "테스터");
        memberService.signup(request);

        assertThatThrownBy(() -> memberService.signup(request))
            .isInstanceOf(MemberAlreadyExistsException.class)
            .hasMessage("이미 존재하는 이메일입니다.");
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        memberService.signup(new SignUpRequest("test@test.com", "1234", "테스터"));

        LoginRequest request = new LoginRequest("test@test.com", "1234");
        LoginResponse response = memberService.login(request);

        assertThat(response.token()).isNotBlank();
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 계정")
    void login_member_not_fount() {
        LoginRequest request = new LoginRequest("not@test.com", "1234");

        assertThatThrownBy(() -> memberService.login(request))
            .isInstanceOf(MemberNotFoundException.class)
            .hasMessage("계정이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_invalid_password() {
        memberService.signup(new SignUpRequest("test@test.com", "1234", "테스터"));

        LoginRequest request = new LoginRequest("test@test.com", "12345");

        assertThatThrownBy(() -> memberService.login(request))
            .isInstanceOf(InvalidPasswordException.class)
            .hasMessage("비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("회원탈퇴 성공")
    void withdraw_success() {
        // given
        SignUpRequest signupRequest = new SignUpRequest("test@example.com", "1234", "테스터");
        SignUpResponse response = memberService.signup(signupRequest);

        Long tokenMemberId = jwtUtil.getMemberId(response.token());
        Long memberId = memberRepository.findByEmail("test@example.com").orElseThrow().getId();

        // when
        memberService.withdraw(memberId, tokenMemberId);

        // then
        assertThat(memberRepository.findByEmail("test@example.com")).isEmpty();
    }

    @Test
    @DisplayName("회원탈퇴 실패 - 존재하지 않는 계정")
    void withdraw_member_not_found() {
        Long dummyTokenMemberId = 10L;
        Long dummyRequestMemberId = 10L;

        assertThatThrownBy(() -> memberService.withdraw(dummyTokenMemberId, dummyRequestMemberId))
            .isInstanceOf(MemberNotFoundException.class)
            .hasMessage("계정이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("회원탈퇴 실패 - 본인 아님 (권한 없음)")
    void withdraw_no_permission() {
        SignUpRequest signupRequest = new SignUpRequest("test@example.com", "1234", "테스터");
        SignUpResponse response = memberService.signup(signupRequest);

        Long tokenMemberId = jwtUtil.getMemberId(response.token());
        Long wrongRequestMemberId = tokenMemberId + 1; // 다른 memberId → 권한 없음 발생

        assertThatThrownBy(() -> memberService.withdraw(tokenMemberId, wrongRequestMemberId))
            .isInstanceOf(NoPermissionException.class)
            .hasMessage("본인 계정이 아닙니다.");
    }

    @Test
    @DisplayName("회원 정보 조회 성공")
    void getMemberInfo_success() {
        // given
        SignUpRequest signupRequest = new SignUpRequest("test@example.com", "1234", "테스터");
        SignUpResponse response = memberService.signup(signupRequest);

        Long tokenMemberId = jwtUtil.getMemberId(response.token());
        Long memberId = memberRepository.findByEmail("test@example.com").orElseThrow().getId();

        // when
        MemberInfoResponse memberInfo = memberService.getMemberInfo(memberId, tokenMemberId);

        // then
        assertThat(memberInfo.email()).isEqualTo("test@example.com");
        assertThat(memberInfo.nickname()).isEqualTo("테스터");
    }

    @Test
    @DisplayName("회원 정보 조회 실패 - 본인 아님 (권한 없음)")
    void getMemberInfo_no_permission() {
        SignUpRequest signupRequest = new SignUpRequest("test@example.com", "1234", "테스터");
        SignUpResponse response = memberService.signup(signupRequest);

        Long tokenMemberId = jwtUtil.getMemberId(response.token());
        Long wrongRequestMemberId = tokenMemberId + 1; // 다른 memberId → 권한 없음 발생

        assertThatThrownBy(() -> memberService.getMemberInfo(tokenMemberId, wrongRequestMemberId))
            .isInstanceOf(NoPermissionException.class)
            .hasMessage("본인 계정이 아닙니다.");
    }
}