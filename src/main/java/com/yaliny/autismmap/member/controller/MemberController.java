package com.yaliny.autismmap.member.controller;

import com.yaliny.autismmap.global.response.BaseResponse;
import com.yaliny.autismmap.member.dto.request.LoginRequest;
import com.yaliny.autismmap.member.dto.request.SignUpRequest;
import com.yaliny.autismmap.member.dto.response.LoginResponse;
import com.yaliny.autismmap.member.dto.response.MemberInfoResponse;
import com.yaliny.autismmap.member.dto.response.SignUpResponse;
import com.yaliny.autismmap.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원 관리 기능")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        LoginResponse response = memberService.login(request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<SignUpResponse>> signup(@RequestBody SignUpRequest request) {
        SignUpResponse response = memberService.signup(request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<String>> logout(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        // 클라이언트에 위임하여 JWT 토큰 삭제
        return ResponseEntity.ok(BaseResponse.success("로그아웃 성공"));
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

}
