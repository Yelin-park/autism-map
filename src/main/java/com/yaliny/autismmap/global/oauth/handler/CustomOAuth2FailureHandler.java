package com.yaliny.autismmap.global.oauth.handler;

import com.yaliny.autismmap.global.exception.CustomException;
import com.yaliny.autismmap.global.oauth.constants.OAuth2Constants;
import com.yaliny.autismmap.global.utils.DeviceExtractor;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2FailureHandler implements AuthenticationFailureHandler {

    private static final String DEFAULT_ERROR_MESSAGE = "소셜 로그인 실패";

    private final DeviceExtractor deviceExtractor;

    @Value("${oauth2.google.front-redirect-uri}")
    private String WEB_REDIRECT_URI;

    @Value("${oauth2.google.app-redirect-uri}")
    private String APP_REDIRECT_URI;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String errorMessage = extractErrorMessage(exception);

        String state = request.getParameter("state");
        String device = extractDeviceFromState(state);

        log.info("[OAuth2FailureHandler] device: {}, error: {}", device, errorMessage);

        // 리다이렉트 URL 생성
        String redirectUrl = buildRedirectUrl(device, errorMessage);

        log.info("[OAuth2FailureHandler] 리다이렉트 URL: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }

    private String extractDeviceFromState(String state) {
        if (state == null || state.isBlank()) return null;

        String[] parts = state.split("\\Q" + OAuth2Constants.STATE_DELIMITER + "\\E");
        for (String p : parts) {
            if (p != null && p.startsWith(OAuth2Constants.DEVICE_PREFIX)) {
                return p.substring(OAuth2Constants.DEVICE_PREFIX.length());
            }
        }
        return null;
    }

    /**
     * 예외에서 에러 메시지 추출
     * 우선순위: CustomException > AuthenticationException message > 기본 메시지
     *
     * @param exception 발생한 예외
     * @return 에러 메시지
     */
    private String extractErrorMessage(AuthenticationException exception) {
        if (exception.getCause() instanceof CustomException customException) {
            return customException.getMessage();
        }

        if (exception.getMessage() != null && !exception.getMessage().isBlank()) {
            return exception.getMessage();
        }

        return DEFAULT_ERROR_MESSAGE;
    }

    /**
     * device에 따라 적절한 에러 리다이렉트 URL 생성
     *
     * @param device 디바이스 타입 (web/app)
     * @param errorMessage 에러 메시지
     * @return 완성된 리다이렉트 URL
     */
    private String buildRedirectUrl(String device, String errorMessage) {
        String baseUri = deviceExtractor.isApp(device) ? APP_REDIRECT_URI : WEB_REDIRECT_URI;

        return UriComponentsBuilder
            .fromUriString(baseUri)
            .queryParam("error", "true")
            .build(true)
            .toUriString();
    }
}
