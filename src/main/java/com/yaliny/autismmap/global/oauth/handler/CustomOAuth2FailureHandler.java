package com.yaliny.autismmap.global.oauth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaliny.autismmap.global.exception.CustomException;
import com.yaliny.autismmap.global.oauth.CustomOAuth2AuthorizationRequestRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomOAuth2FailureHandler implements AuthenticationFailureHandler {

    private final CustomOAuth2AuthorizationRequestRepository authRequestRepository;

    @Value("${oauth2.google.front-redirect-uri}")
    private String WEB_REDIRECT_URI;

    @Value("${oauth2.google.app-redirect-uri}")
    private String APP_REDIRECT_URI;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String message = "소셜 로그인 실패";
        if (exception.getCause() instanceof CustomException customException) {
            message = customException.getMessage();
        } else if (exception.getMessage() != null) {
            message = exception.getMessage();
        }

        String device = authRequestRepository.getDevice(request);
        boolean isApp = "app".equalsIgnoreCase(device);

        String base = isApp ? APP_REDIRECT_URI : WEB_REDIRECT_URI;

        String redirectUrl = org.springframework.web.util.UriComponentsBuilder
            .fromUriString(base)
            .queryParam("error", "true")
            .queryParam("message", URLEncoder.encode(message, StandardCharsets.UTF_8))
            .build(true)
            .toUriString();

        response.sendRedirect(redirectUrl);
    }
}
