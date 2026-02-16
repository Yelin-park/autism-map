package com.yaliny.autismmap.member.controller;

import com.yaliny.autismmap.global.binder.CookieProperties;
import com.yaliny.autismmap.global.response.BaseResponse;
import com.yaliny.autismmap.global.security.CustomUserDetails;
import com.yaliny.autismmap.member.dto.request.LoginRequest;
import com.yaliny.autismmap.member.dto.request.PasswordRequest;
import com.yaliny.autismmap.member.dto.request.SignUpRequest;
import com.yaliny.autismmap.member.dto.response.LoginResponse;
import com.yaliny.autismmap.member.dto.response.MemberInfoResponse;
import com.yaliny.autismmap.member.dto.response.SignUpResponse;
import com.yaliny.autismmap.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원 관리 기능")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final CookieProperties cookieProperties;

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        LoginResponse response = memberService.login(request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<SignUpResponse>> signup(@RequestBody @Valid SignUpRequest request) {
        SignUpResponse response = memberService.signup(request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public BaseResponse<Void> logout(HttpServletResponse response) {

        ResponseCookie cookie = ResponseCookie.from("ACCESS_TOKEN", "")
            .httpOnly(true)
            .secure(cookieProperties.isSecure())
            .sameSite(cookieProperties.getSameSite())
            .path("/")
            .maxAge(0) // 즉시 만료
            .domain(
                cookieProperties.getDomain() == null || cookieProperties.getDomain().isBlank()
                    ? null
                    : cookieProperties.getDomain()
            )
            .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return BaseResponse.success();
    }

    @Operation(summary = "회원탈퇴")
    @DeleteMapping("/{memberId}")
    public ResponseEntity<BaseResponse<String>> withdraw(
        @PathVariable long memberId
    ) {
        memberService.withdraw(memberId);
        return ResponseEntity.ok(BaseResponse.success("회원탈퇴 성공"));
    }

    @Operation(summary = "회원 정보 조회")
    @GetMapping("/{memberId}")
    public ResponseEntity<BaseResponse<MemberInfoResponse>> getMemberInfo(
        @PathVariable long memberId
    ) {
        MemberInfoResponse response = memberService.getMemberInfo(memberId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "닉네임 수정")
    @PatchMapping("/{memberId}/nickname")
    public ResponseEntity<BaseResponse<MemberInfoResponse>> updateNickname(
        @PathVariable Long memberId,
        @RequestParam String nickname
    ) {
        MemberInfoResponse response = memberService.updateNickname(memberId, nickname);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "비밀번호 변경")
    @PatchMapping("/{memberId}/password")
    public ResponseEntity<BaseResponse<MemberInfoResponse>> updatePassword(
            @PathVariable Long memberId,
            @RequestBody @Valid PasswordRequest request
    ) {
        MemberInfoResponse response = memberService.updatePassword(memberId, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "로그인 사용자 전용 내정보 조회")
    @GetMapping("/me")
    public ResponseEntity<BaseResponse<MemberInfoResponse>> getMyInfo(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MemberInfoResponse response = memberService.getMemberInfo(userDetails.getMemberId());
        return ResponseEntity.ok(BaseResponse.success(response));
    }

}
