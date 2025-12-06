/*
package com.yaliny.autismmap.global.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

public class CustomOAuth2AuthorizationRequestRepository extends HttpSessionOAuth2AuthorizationRequestRepository {

    private static final String DEVICE_PARAM = "device";
    private static final String SESSION_DEVICE_KEY = "OAUTH2_DEVICE";

    @Override
    public void saveAuthorizationRequest(
        OAuth2AuthorizationRequest authorizationRequest,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        if (authorizationRequest != null) {
            String device = request.getParameter(DEVICE_PARAM);
            if (device != null) {
                request.getSession().setAttribute(SESSION_DEVICE_KEY, device);
            }
        }
        super.saveAuthorizationRequest(authorizationRequest, request, response);
    }

    public String getDevice(HttpServletRequest request) {
        Object device = request.getSession().getAttribute(SESSION_DEVICE_KEY);
        return device != null ? device.toString() : null;
    }
}
*/
