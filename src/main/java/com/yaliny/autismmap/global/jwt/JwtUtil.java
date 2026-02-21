package com.yaliny.autismmap.global.jwt;

import com.yaliny.autismmap.member.entity.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JWT 토큰 생성, 검증, 토큰에서 사용자 정보 꺼내는 역할을 한다.
 */
@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 토큰 생성
     */
    public String generateToken(long memberId, String email, String role) {
        return Jwts.builder()
            .setSubject(String.valueOf(memberId))
            .claim("email", email) // email 정보 추가
            .claim("role", role) // ROLE 정보 추가
            .setIssuedAt(new Date()) // 발급 시각
            .setExpiration(new Date(System.currentTimeMillis() + expiration)) // 만료시간
            .signWith(key, SignatureAlgorithm.HS256) // 서명
            .compact();
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token); // 파싱이 가능해야 유효한 토큰
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("[JwtUtil] 토큰 만료: {}", e.getMessage());
            throw e;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("[JwtUtil] 유효하지 않은 토큰: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 토큰에서 Claims 꺼내기
     */
    public Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public String getEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    public Role getRole(String token) {
        return Role.valueOf(getClaims(token).get("role", String.class));
    }

    public Long getMemberId(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

}
