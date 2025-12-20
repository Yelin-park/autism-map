package com.yaliny.autismmap.global.oauth.handler;

import com.yaliny.autismmap.global.exception.CustomException;
import com.yaliny.autismmap.global.exception.ErrorCode;
import com.yaliny.autismmap.global.jwt.JwtUtil;
import com.yaliny.autismmap.global.oauth.CustomOAuth2AuthorizationRequestRepository;
import com.yaliny.autismmap.member.entity.Member;
import com.yaliny.autismmap.member.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final CustomOAuth2AuthorizationRequestRepository authRequestRepository;

    @Value("${oauth2.google.front-redirect-uri}")
    private String WEB_REDIRECT_URI;

    @Value("${oauth2.google.app-redirect-uri}")
    private String APP_REDIRECT_URI;

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = (String) oAuth2User.getAttributes().get("email");

        Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        String token = jwtUtil.generateToken(member.getId(), member.getEmail(), member.getRole().name());

        // 프론트(WebView)에서 로그인 버튼 클릭 시 ?device=app 붙여서 요청
        String device = authRequestRepository.getDevice(request);
        log.info("[OAuth2SuccessHandler] device: {}", device);

        boolean isApp = "app".equalsIgnoreCase(device);
        String base = isApp ? APP_REDIRECT_URI : WEB_REDIRECT_URI;

        String redirectUrl = org.springframework.web.util.UriComponentsBuilder
            .fromUriString(base)
            .queryParam("token", token)
            .build(true)
            .toUriString();

        log.info("[OAuth2SuccessHandler] Redirecting to: {}", redirectUrl);

        response.sendRedirect(redirectUrl);
    }
}
