package com.yaliny.autismmap.member.controller;

import com.yaliny.autismmap.global.response.BaseResponse;
import com.yaliny.autismmap.member.service.OAuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "OAuth 기능")
@RestController
@RequestMapping("/api/v1/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthService oAuthService;

    @GetMapping("/kakao")
    public ResponseEntity<BaseResponse<String>> kakaoLogin(@RequestParam("code") String code) {
        String jwt = oAuthService.kakaoLogin(code); // 토큰 발급 및 로그인 처리
        return ResponseEntity.ok(BaseResponse.success(jwt));
    }

}
