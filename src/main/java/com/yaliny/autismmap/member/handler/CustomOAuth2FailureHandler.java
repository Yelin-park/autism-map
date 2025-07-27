package com.yaliny.autismmap.member.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaliny.autismmap.global.exception.CustomException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
public class CustomOAuth2FailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${front.url}")
    private String frontUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String message = "소셜 로그인 실패";

        if (exception.getCause() instanceof CustomException customException) {
            message = customException.getMessage();
        } else if (exception.getMessage() != null) {
            message = exception.getMessage();
        }

        String redirectUrl = frontUrl + "/oauth?error=true&message=" + URLEncoder.encode(message, StandardCharsets.UTF_8);
        response.sendRedirect(redirectUrl);
        response.flushBuffer();
    }
}
