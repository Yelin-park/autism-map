package com.yaliny.autismmap.member.controller;

import com.yaliny.autismmap.global.jwt.JwtUtil;
import com.yaliny.autismmap.global.response.BaseResponse;
import com.yaliny.autismmap.member.dto.*;
import com.yaliny.autismmap.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    private final JwtUtil jwtUtil;
    private final MemberService memberService;

    @Operation(summary = "로그인",
        responses = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "비밀번호 불일치"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 이메일"),
        }
    )
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        LoginResponse response = memberService.login(request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "회원가입",
        responses = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일")
        }
    )
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
        @PathVariable Long memberId,
        @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = authorizationHeader.substring(7);
        Long tokenMemberId = jwtUtil.getMemberId(token);

        memberService.withdraw(memberId, tokenMemberId);
        return ResponseEntity.ok(BaseResponse.success("회원탈퇴 성공"));
    }

    @Operation(summary = "회원 정보 조회")
    @GetMapping("/{memberId}")
    public ResponseEntity<BaseResponse<MemberInfoResponse>> getMemberInfo(
        @PathVariable Long memberId,
        @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = authorizationHeader.substring(7);
        Long tokenMemberId = jwtUtil.getMemberId(token);

        MemberInfoResponse response = memberService.getMemberInfo(memberId, tokenMemberId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}
