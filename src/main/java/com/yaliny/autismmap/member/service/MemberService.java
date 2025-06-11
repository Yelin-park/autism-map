package com.yaliny.autismmap.member.service;

import com.yaliny.autismmap.global.jwt.JwtUtil;
import com.yaliny.autismmap.member.dto.LoginResponse;
import com.yaliny.autismmap.member.entity.Member;
import com.yaliny.autismmap.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(String email, String password) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("계정이 존재하지 않습니다."));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtUtil.generateToken(member.getEmail(), member.getRole().name());
        return new LoginResponse(token);
    }
}
