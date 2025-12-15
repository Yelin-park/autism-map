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

    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;

    public CustomOAuth2AuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository,
                "/oauth2/authorization"
        );
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest originalRequest = defaultResolver.resolve(request);
        return customize(request, originalRequest);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientId) {
        OAuth2AuthorizationRequest originalRequest = defaultResolver.resolve(request, clientId);
        return customize(request, originalRequest);
    }

    private OAuth2AuthorizationRequest customize(
        HttpServletRequest request,
        OAuth2AuthorizationRequest originalRequest
    ) {
        if (originalRequest == null) return null;
        String device = request.getParameter(DEVICE_PARAM);
        log.debug("[CustomOAuth2AuthorizationRequestResolver] device: {}", device);

        if (device == null) {
            return originalRequest;
        }

        Map<String, Object> additionalParams = new HashMap<>(originalRequest.getAdditionalParameters());
        additionalParams.put(DEVICE_PARAM, device);

        return OAuth2AuthorizationRequest.from(originalRequest)
            .additionalParameters(additionalParams)
            .build();
    }
}
