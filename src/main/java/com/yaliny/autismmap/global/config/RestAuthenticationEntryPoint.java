package com.yaliny.autismmap.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaliny.autismmap.global.exception.ErrorCode;
import com.yaliny.autismmap.global.response.BaseResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");

        BaseResponse<Void> body = BaseResponse.error(
            HttpStatus.UNAUTHORIZED.value(), ErrorCode.UNAUTHORIZED.getMessage()
        );

        objectMapper.writeValue(response.getOutputStream(), body);
    }

}
