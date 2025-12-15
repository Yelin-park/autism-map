package com.yaliny.autismmap.global.oauth.handler;

import com.yaliny.autismmap.global.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class CustomOAuth2FailureHandler implements AuthenticationFailureHandler {

    @Value("${front.url}")
    private String frontUrl;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        String message = resolveMessage(exception);

        String redirectUrl = UriComponentsBuilder
                .fromUriString(frontUrl)
                .path("/oauth")
                .queryParam("error", true)
                .queryParam("message", message)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUriString();

        response.sendRedirect(redirectUrl);
    }

    private String resolveMessage(AuthenticationException exception) {
        Throwable cause = exception.getCause();

        if (cause instanceof CustomException customException) {
            return customException.getMessage();
        }

        return exception.getMessage() != null ? exception.getMessage() : "소셜 로그인 실패";
    }
}
