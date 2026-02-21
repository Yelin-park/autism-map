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
        Exception exception = (Exception) request.getAttribute("exception");
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        if (exception instanceof io.jsonwebtoken.ExpiredJwtException) {
            errorCode = ErrorCode.EXPIRED_TOKEN;
        } else if (exception instanceof io.jsonwebtoken.security.SignatureException) {
            errorCode = ErrorCode.INVALID_SIGNATURE;
        } else if (exception instanceof io.jsonwebtoken.MalformedJwtException) {
            errorCode = ErrorCode.MALFORMED_TOKEN;
        } else if (exception instanceof io.jsonwebtoken.UnsupportedJwtException) {
            errorCode = ErrorCode.UNSUPPORTED_TOKEN;
        } else if (exception instanceof IllegalArgumentException) {
            errorCode = ErrorCode.EMPTY_TOKEN;
        }

        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        BaseResponse<Void> body = BaseResponse.error(
            errorCode.getStatus().value(), errorCode.getMessage()
        );

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
