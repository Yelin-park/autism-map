package com.yaliny.autismmap.global.oauth;

import com.yaliny.autismmap.global.oauth.constants.OAuth2Constants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

/**
 * OAuth2 인증 요청 커스터마이징
 * - state 파라미터에 device 정보 추가
 * - 형식: "random_state|device=app" 또는 "random_state|device=web"
 */
@Slf4j
@Component
public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private static final String AUTHORIZATION_BASE_URI = "/oauth2/authorization";

    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;

    public CustomOAuth2AuthorizationRequestResolver(ClientRegistrationRepository repo) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
            repo,
            AUTHORIZATION_BASE_URI
        );
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest originalRequest = defaultResolver.resolve(request);
        return customizeAuthorizationRequest(request, originalRequest);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientId) {
        OAuth2AuthorizationRequest originalRequest = defaultResolver.resolve(request, clientId);
        return customizeAuthorizationRequest(request, originalRequest);
    }

    /**
     * 인증 요청을 커스터마이징하여 state에 device 정보 추가
     *
     * @param request HTTP 요청
     * @param originalRequest 원본 OAuth2 인증 요청
     * @return 커스터마이징된 OAuth2 인증 요청
     */
    private OAuth2AuthorizationRequest customizeAuthorizationRequest(
        HttpServletRequest request,
        OAuth2AuthorizationRequest originalRequest
    ) {
        if (originalRequest == null) {
            return null;
        }

        String device = request.getParameter(OAuth2Constants.DEVICE_PARAM);
        log.info("[CustomOAuth2AuthorizationRequestResolver] device param: {}", device);

        // device 파라미터가 없거나 비어있으면 원본 요청 그대로 반환
        if (device == null || device.isBlank()) {
            log.debug("[CustomOAuth2AuthorizationRequestResolver] device 파라미터가 없어 원본 요청 사용");
            return originalRequest;
        }

        // device가 유효한 값인지 검증
        if (!isValidDevice(device)) {
            log.warn("[CustomOAuth2AuthorizationRequestResolver] 유효하지 않은 device 값: {}", device);
            return originalRequest;
        }

        // ✅ OAuth 왕복 보장 값인 state에 device를 붙여서 callback까지 가져간다
        String baseState = originalRequest.getState();
        String newState = baseState + OAuth2Constants.STATE_DELIMITER + OAuth2Constants.DEVICE_PREFIX + device;

        return OAuth2AuthorizationRequest.from(originalRequest)
            .state(newState)
            .build();
    }

    /**
     * device 값이 유효한지 검증
     *
     * @param device device 값
     * @return 유효하면 true
     */
    private boolean isValidDevice(String device) {
        return OAuth2Constants.DEVICE_WEB.equalsIgnoreCase(device)
            || OAuth2Constants.DEVICE_APP.equalsIgnoreCase(device);
    }

    /**
     * 원본 state에 device 정보를 추가한 새로운 state 생성
     *
     * @param baseState 원본 state
     * @param device device 값
     * @return 향상된 state
     */
    private String buildEnhancedState(String baseState, String device) {
        return baseState + OAuth2Constants.STATE_DELIMITER
            + OAuth2Constants.DEVICE_PREFIX + device;
    }
}
