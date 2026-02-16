package com.yaliny.autismmap.global.oauth.handler;

import com.yaliny.autismmap.global.binder.CookieProperties;
import com.yaliny.autismmap.global.exception.CustomException;
import com.yaliny.autismmap.global.exception.ErrorCode;
import com.yaliny.autismmap.global.jwt.JwtUtil;
import com.yaliny.autismmap.global.oauth.constants.OAuth2Constants;
import com.yaliny.autismmap.global.utils.DeviceExtractor;
import com.yaliny.autismmap.member.entity.Member;
import com.yaliny.autismmap.member.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final DeviceExtractor deviceExtractor;
    private final CookieProperties cookieProperties;

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
        log.info("[OAuth2SuccessHandler] 인증 성공 처리 시작");

        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = (String) oAuth2User.getAttributes().get("email");

            if (email == null || email.isBlank()) {
                log.error("[OAuth2SuccessHandler] 이메일 정보가 없습니다.");
                throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
            }

            Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

            String token = jwtUtil.generateToken(member.getId(), member.getEmail(), member.getRole().name());

            // ✅ 세션 대신, state에서 device를 추출 (외부 브라우저에서도 안정적)
            String state = request.getParameter("state");
            String device = extractDeviceFromState(state);
            log.info("[OAuth2SuccessHandler] device(from state): {}", device);

            // 리다이렉트 URL 생성
            String redirectUrl = buildRedirectUrl(device, token);

            log.info("[OAuth2SuccessHandler] Redirecting to: {}", redirectUrl);
            if (deviceExtractor.isApp(device)) {
                response.sendRedirect(buildRedirectUrl(device, token));
                return;
            }

            // 웹은 쿠키로 전달
            ResponseCookie cookie = ResponseCookie.from("ACCESS_TOKEN", token)
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .sameSite(cookieProperties.getSameSite())
                .path("/")
                .maxAge(cookieProperties.getMaxAge())
                .domain(cookieProperties.getDomain() == null || cookieProperties.getDomain().isBlank()
                    ? null
                    : cookieProperties.getDomain())
                .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            response.sendRedirect(WEB_REDIRECT_URI);

        } catch (CustomException e) {
            log.error("[OAuth2SuccessHandler] CustomException 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[OAuth2SuccessHandler] 예상치 못한 오류 발생", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private String extractDeviceFromState(String state) {
        if (state == null || state.isBlank()) return null;

        // state = <random>|device=app 형태
        // 구분자(|) 기준으로 토큰 분리해서 device= 로 시작하는 조각을 찾는다
        String[] parts = state.split("\\Q" + OAuth2Constants.STATE_DELIMITER + "\\E");
        for (String p : parts) {
            if (p != null && p.startsWith(OAuth2Constants.DEVICE_PREFIX)) {
                return p.substring(OAuth2Constants.DEVICE_PREFIX.length());
            }
        }
        return null;
    }

    /**
     * device에 따라 적절한 리다이렉트 URL 생성
     *
     * @param device 디바이스 타입 (web/app)
     * @param token JWT 토큰
     * @return 완성된 리다이렉트 URL
     */
    private String buildRedirectUrl(String device, String token) {
        if (deviceExtractor.isApp(device)) {
            return UriComponentsBuilder
                .fromUriString(APP_REDIRECT_URI)
                .queryParam("token", token)
                .build(true)
                .toUriString();
        }

        // 웹은 토큰을 URL에 붙이지 않음
        return WEB_REDIRECT_URI;
    }

}
