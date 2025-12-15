package com.yaliny.autismmap.global.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

@Component
public class CustomOAuth2AuthorizationRequestRepository
    implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String SESSION_DEVICE_KEY = "OAUTH2_DEVICE";
    private static final String DEVICE_PARAM = "device";

    // 기존 세션 Repository를 내부에서 사용 (상속 불가 → 구성 방식)
    private final HttpSessionOAuth2AuthorizationRequestRepository delegate =
        new HttpSessionOAuth2AuthorizationRequestRepository();

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return delegate.loadAuthorizationRequest(request);
    }

    @Override
    public void saveAuthorizationRequest(
        OAuth2AuthorizationRequest authorizationRequest,
        HttpServletRequest request,
        HttpServletResponse response) {

        if (authorizationRequest != null) {
            String device = request.getParameter(DEVICE_PARAM);
            if (device != null) {
                request.getSession().setAttribute(SESSION_DEVICE_KEY, device);
            }
        }

        delegate.saveAuthorizationRequest(authorizationRequest, request, response);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        request.getSession().removeAttribute(SESSION_DEVICE_KEY);
        return delegate.removeAuthorizationRequest(request, response);
    }

    // SuccessHandler 에서 device 를 조회하기 위한 메서드
    public String getDevice(HttpServletRequest request) {
        Object device = request.getSession().getAttribute(SESSION_DEVICE_KEY);
        return device != null ? device.toString() : null;
    }
}
