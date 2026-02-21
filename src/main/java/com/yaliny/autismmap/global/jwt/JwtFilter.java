package com.yaliny.autismmap.global.jwt;

import com.yaliny.autismmap.global.security.CustomUserDetails;
import com.yaliny.autismmap.member.entity.Role;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String requestUri = request.getRequestURI();

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

        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            log.warn("[JwtFilter] JWT 인증 실패: {} - {}", requestUri, e.getMessage());
            request.setAttribute("exception", e);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("[JwtFilter] 예상치 못한 오류 발생: {}", requestUri, e);
            request.setAttribute("exception", e);
            filterChain.doFilter(request, response);
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
        Claims claims = jwtUtil.getClaims(token);

        Long memberId = Long.parseLong(claims.getSubject());
        String email = claims.get("email", String.class);
        String roleName = claims.get("role", String.class);
        Role role = Role.valueOf(roleName);

        CustomUserDetails userDetails = new CustomUserDetails(
            memberId,
            email,
            roleName,
            List.of(new SimpleGrantedAuthority(role.getAuthority()))
        );

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/h2-console") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.endsWith(".ico") ||
               path.endsWith(".png") ||
               path.endsWith(".jpg");
    }
}
