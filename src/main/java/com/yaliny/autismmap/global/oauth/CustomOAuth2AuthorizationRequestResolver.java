package com.yaliny.autismmap.global.oauth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.util.HashMap;
import java.util.Map;

public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

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

        String device = request.getParameter("device");

        Map<String, Object> additionalParams = new HashMap<>(originalRequest.getAdditionalParameters());
        if (device != null) {
            additionalParams.put("device", device);
        }

        return OAuth2AuthorizationRequest.from(originalRequest)
            .additionalParameters(additionalParams)
            .build();
    }
}
