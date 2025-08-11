package com.yaliny.autismmap.member.service;

import com.yaliny.autismmap.global.exception.CustomException;
import com.yaliny.autismmap.global.jwt.JwtUtil;
import com.yaliny.autismmap.global.security.CustomUserDetails;
import com.yaliny.autismmap.member.dto.request.LoginRequest;
import com.yaliny.autismmap.member.dto.request.SignUpRequest;
import com.yaliny.autismmap.member.dto.response.LoginResponse;
import com.yaliny.autismmap.member.dto.response.MemberInfoResponse;
import com.yaliny.autismmap.member.dto.response.SignUpResponse;
import com.yaliny.autismmap.member.entity.Member;
import com.yaliny.autismmap.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
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
            .isInstanceOf(CustomException.class)
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
            .isInstanceOf(CustomException.class)
            .hasMessage("계정이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_invalid_password() {
        memberService.signup(new SignUpRequest("test@test.com", "1234", "테스터"));

        LoginRequest request = new LoginRequest("test@test.com", "12345");

        assertThatThrownBy(() -> memberService.login(request))
            .isInstanceOf(CustomException.class)
            .hasMessage("비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("회원탈퇴 성공")
    void withdraw_success() {
        // given
        SignUpRequest signupRequest = new SignUpRequest("test@example.com", "1234", "테스터");
        SignUpResponse response = memberService.signup(signupRequest);

        Member member = memberRepository.findByEmail("test@example.com").get();
        setAuthentication(member);

        // when
        memberService.withdraw(member.getId());

        // then
        assertThat(memberRepository.findByEmail("test@example.com")).isEmpty();
    }

    @Test
    @DisplayName("회원탈퇴 실패 - 존재하지 않는 계정")
    void withdraw_member_not_found() {
        Long dummyRequestMemberId = 10L;

        assertThatThrownBy(() -> memberService.withdraw(dummyRequestMemberId))
            .isInstanceOf(CustomException.class)
            .hasMessage("계정이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("회원탈퇴 실패 - 본인 아님 (권한 없음)")
    void withdraw_no_permission() {
        SignUpRequest signupRequest = new SignUpRequest("test@example.com", "1234", "테스터");
        memberService.signup(signupRequest);

        SignUpRequest signupRequest2 = new SignUpRequest("test@example1.com", "1234", "테스터2");
        memberService.signup(signupRequest2);

        Member member = memberRepository.findByEmail("test@example.com").get();
        Member member2 = memberRepository.findByEmail("test@example1.com").get();
        setAuthentication(member2);

        assertThatThrownBy(() -> memberService.withdraw(member.getId()))
            .isInstanceOf(CustomException.class)
            .hasMessage("접근 권한이 없습니다.");

        clearAuthentication();
    }

    @Test
    @DisplayName("회원 정보 조회 성공")
    void getMemberInfo_success() {
        // given
        SignUpRequest signupRequest = new SignUpRequest("test@example.com", "1234", "테스터");
        memberService.signup(signupRequest);

        Member member = memberRepository.findByEmail("test@example.com").get();
        setAuthentication(member);

        // when
        MemberInfoResponse memberInfo = memberService.getMemberInfo(member.getId());

        // then
        assertThat(memberInfo.email()).isEqualTo("test@example.com");
        assertThat(memberInfo.nickname()).isEqualTo("테스터");
    }

    @Test
    @DisplayName("회원 정보 조회 실패 - 본인 아님 (권한 없음)")
    void getMemberInfo_no_permission() {
        SignUpRequest signupRequest = new SignUpRequest("test@example.com", "1234", "테스터");
        memberService.signup(signupRequest);

        SignUpRequest signupRequest2 = new SignUpRequest("test@example1.com", "1234", "테스터2");
        memberService.signup(signupRequest2);

        Member member = memberRepository.findByEmail("test@example.com").get();
        Member member2 = memberRepository.findByEmail("test@example1.com").get();
        setAuthentication(member2);

        assertThatThrownBy(() -> memberService.getMemberInfo(member.getId()))
            .isInstanceOf(CustomException.class)
            .hasMessage("접근 권한이 없습니다.");

        clearAuthentication();
    }
}