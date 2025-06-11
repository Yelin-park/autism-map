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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원 관리 기능")
@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/login")
    @Operation(summary = "로그인",
        responses = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "비밀번호 불일치"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 이메일"),
        }
    )
    public ResponseEntity<BaseResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        LoginResponse response = memberService.login(request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PostMapping("/signup")
    @Operation(summary = "회원가입",
        responses = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일")
        }
    )
    public ResponseEntity<BaseResponse<SignUpResponse>> signup(@RequestBody SignUpRequest request) {
        SignUpResponse response = memberService.signup(request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}
