package com.yaliny.autismmap.member.controller;

import com.yaliny.autismmap.global.response.BaseResponse;
import com.yaliny.autismmap.member.dto.LoginRequest;
import com.yaliny.autismmap.member.dto.LoginResponse;
import com.yaliny.autismmap.member.dto.SignUpRequest;
import com.yaliny.autismmap.member.dto.SignUpResponse;
import com.yaliny.autismmap.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        LoginResponse response = memberService.login(request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<SignUpResponse>> signup(@RequestBody SignUpRequest request) {
        SignUpResponse response = memberService.signup(request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}
