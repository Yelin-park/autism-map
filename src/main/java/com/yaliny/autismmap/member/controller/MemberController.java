package com.yaliny.autismmap.member.controller;

import com.yaliny.autismmap.global.response.BaseResponse;
import com.yaliny.autismmap.member.dto.LoginRequest;
import com.yaliny.autismmap.member.dto.LoginResponse;
import com.yaliny.autismmap.member.dto.SignUpRequest;
import com.yaliny.autismmap.member.dto.SignUpResponse;
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
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class MemberController {

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
    @DeleteMapping
    public ResponseEntity<BaseResponse<String>> withdraw(Authentication authentication) {
        memberService.withdraw((String) authentication.getPrincipal());
        return ResponseEntity.ok(BaseResponse.success("회원탈퇴 성공"));
    }
}
