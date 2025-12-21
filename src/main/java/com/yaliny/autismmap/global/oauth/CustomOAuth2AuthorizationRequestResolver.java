package com.yaliny.autismmap.global.oauth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private static final String DEVICE_PARAM = "device";
    private static final String STATE_DELIM = "|";
    private static final String DEVICE_PREFIX = "device=";

    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;

    public CustomOAuth2AuthorizationRequestResolver(ClientRegistrationRepository repo) {
        this.defaultResolver =
            new DefaultOAuth2AuthorizationRequestResolver(repo, "/oauth2/authorization");
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

    private OAuth2AuthorizationRequest customizeAuthorizationRequest(
        HttpServletRequest request,
        OAuth2AuthorizationRequest originalRequest
    ) {
        if (originalRequest == null) return null;

        String device = request.getParameter(DEVICE_PARAM);
        log.info("[CustomOAuth2AuthorizationRequestResolver] device: {}", device);

        if (device == null || device.isBlank()) {
            return originalRequest;
        }

        // ✅ OAuth 왕복 보장 값인 state에 device를 붙여서 callback까지 가져간다
        String baseState = originalRequest.getState();
        String newState = baseState + STATE_DELIM + DEVICE_PREFIX + device;

        return OAuth2AuthorizationRequest.from(originalRequest)
            .state(newState)
            .build();
    }
}
