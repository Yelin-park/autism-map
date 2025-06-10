package com.yaliny.autismmap.global.jwt;

import com.yaliny.autismmap.member.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JWT 토큰 생성, 검증, 토큰에서 사용자 정보 꺼내는 역할을 한다.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private final Key key = Keys.hmacShaKeyFor(secret.getBytes());

    /**
     * 토큰 생성
     */
    public String generateToken(String email, String role) {
        return Jwts.builder()
            .setSubject(email) // subject는 email
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
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
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
        return getClaims(token).getSubject();
    }

    public Role getRole(String token) {
        return Role.valueOf(getClaims(token).get("role", String.class));
    }

}
