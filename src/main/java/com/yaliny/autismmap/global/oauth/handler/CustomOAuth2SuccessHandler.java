package com.yaliny.autismmap.global.oauth.handler;

import com.yaliny.autismmap.global.exception.CustomException;
import com.yaliny.autismmap.global.exception.ErrorCode;
import com.yaliny.autismmap.global.jwt.JwtUtil;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private static final String STATE_DELIM = "|";
    private static final String DEVICE_PREFIX = "device=";

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;

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

        // ✅ 세션 대신, state에서 device를 추출 (외부 브라우저에서도 안정적)
        String device = extractDeviceFromState(request.getParameter("state"));
        log.info("[OAuth2SuccessHandler] device(from state): {}", device);

        boolean isApp = "app".equalsIgnoreCase(device);
        String base = isApp ? APP_REDIRECT_URI : WEB_REDIRECT_URI;

        String redirectUrl = UriComponentsBuilder
            .fromUriString(base)
            .queryParam("token", token)
            .build(true)
            .toUriString();

        log.info("[OAuth2SuccessHandler] Redirecting to: {}", redirectUrl);

        response.sendRedirect(redirectUrl);
    }

    private String extractDeviceFromState(String state) {
        if (state == null || state.isBlank()) return null;

        // state = <random>|device=app 형태
        // 구분자(|) 기준으로 토큰 분리해서 device= 로 시작하는 조각을 찾는다
        String[] parts = state.split("\\Q" + STATE_DELIM + "\\E");
        for (String p : parts) {
            if (p != null && p.startsWith(DEVICE_PREFIX)) {
                return p.substring(DEVICE_PREFIX.length());
            }
        }
        return null;
    }

}
