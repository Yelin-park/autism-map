package com.yaliny.autismmap.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaliny.autismmap.global.response.BaseResponse;
import com.yaliny.autismmap.global.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 인증 필터
 * - Authorization 헤더에서 JWT 토큰을 추출하고 검증
 * - 유효한 토큰인 경우 SecurityContext에 인증 정보 설정
 * - 토큰이 없거나 유효하지 않은 경우 요청을 그대로 통과시킴 (Spring Security가 처리)
 *
 * 역할
 * 모든 요청에 대해 Authorization 헤더에 JWT가 있는지 확인
 * 토큰이 유효하다면 → 사용자 정보(SecurityContext)에 저장
 * 인증된 사용자만 접근할 수 있도록 만들기
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ROLE_PREFIX = "ROLE_";

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        log.debug("[JwtFilter] 요청 URI: {}", requestUri);

        try {
            // 1. 헤더에서 토큰 추출
            String token = extractToken(request);

            // 2. 토큰이 있고 유효한 경우 인증 정보 설정
            if (token != null && jwtUtil.validateToken(token)) {
                setAuthentication(token, request);
                log.debug("[JwtFilter] 인증 성공: {}", requestUri);
            }

            // 3. 다음 필터로 요청 전달
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            log.warn("[JwtFilter] 토큰 만료: {}", requestUri);
            handleJwtException(response, "토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED);
        } catch (MalformedJwtException e) {
            log.warn("[JwtFilter] 잘못된 토큰 형식: {}", requestUri);
            handleJwtException(response, "유효하지 않은 토큰 형식입니다.", HttpStatus.UNAUTHORIZED);
        } catch (UnsupportedJwtException e) {
            log.warn("[JwtFilter] 지원하지 않는 토큰: {}", requestUri);
            handleJwtException(response, "지원하지 않는 토큰입니다.", HttpStatus.UNAUTHORIZED);
        } catch (SignatureException e) {
            log.warn("[JwtFilter] 토큰 서명 오류: {}", requestUri);
            handleJwtException(response, "토큰 서명이 유효하지 않습니다.", HttpStatus.UNAUTHORIZED);
        } catch (IllegalArgumentException e) {
            log.warn("[JwtFilter] 토큰 정보 없음: {}", requestUri);
            handleJwtException(response, "토큰 정보가 없습니다.", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("[JwtFilter] 예상치 못한 오류 발생: {}", requestUri, e);
            handleJwtException(response, "인증 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Authorization 헤더에서 JWT 토큰 추출
     *
     * @param request HTTP 요청
     * @return JWT 토큰 문자열, 없으면 null
     */
    private String extractToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            return authorizationHeader.substring(BEARER_PREFIX.length());
        }

        // 헤더 없으면 쿠키에서 찾기
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("ACCESS_TOKEN".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * JWT 토큰을 검증하고 SecurityContext에 인증 정보 설정
     *
     * @param token JWT 토큰
     * @param request HTTP 요청
     */
    private void setAuthentication(String token, HttpServletRequest request) {
        try {
            Claims claims = jwtUtil.getClaims(token);

            String memberId = claims.getSubject();
            String email = claims.get("email", String.class);
            String role = claims.get("role", String.class);

            // UserDetails 생성
            CustomUserDetails userDetails = new CustomUserDetails(
                Long.parseLong(memberId),
                email,
                role,
                List.of(new SimpleGrantedAuthority(ROLE_PREFIX + role))
            );

            // 인증 객체 생성
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );

            authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
            );

            // SecurityContext에 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("[JwtFilter] 인증 정보 설정 완료 - memberId: {}, role: {}", memberId, role);

        } catch (Exception e) {
            log.error("[JwtFilter] 인증 정보 설정 실패", e);
            SecurityContextHolder.clearContext();
            throw e;
        }
    }

    /**
     * JWT 예외 처리 - JSON 형식의 에러 응답 반환
     *
     * @param response HTTP 응답
     * @param message 에러 메시지
     * @param status HTTP 상태 코드
     */
    private void handleJwtException(
        HttpServletResponse response,
        String message,
        HttpStatus status
    ) throws IOException {

        SecurityContextHolder.clearContext();

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        BaseResponse<Void> errorResponse = BaseResponse.error(
            status.value(),
            message
        );

        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }

    /**
     * 특정 경로에 대해 필터를 건너뛸지 결정
     * 현재는 모든 요청에 대해 필터 적용
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // 필요시 특정 경로는 필터를 건너뛰도록 설정 가능
        // 예: return path.startsWith("/public");

        return false;
    }
}
