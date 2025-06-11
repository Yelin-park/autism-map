package com.yaliny.autismmap.member.service;

import com.yaliny.autismmap.global.exception.InvalidPasswordException;
import com.yaliny.autismmap.global.exception.MemberAlreadyExistsException;
import com.yaliny.autismmap.global.exception.MemberNotFoundException;
import com.yaliny.autismmap.global.jwt.JwtUtil;
import com.yaliny.autismmap.member.dto.LoginRequest;
import com.yaliny.autismmap.member.dto.LoginResponse;
import com.yaliny.autismmap.member.dto.SignUpRequest;
import com.yaliny.autismmap.member.dto.SignUpResponse;
import com.yaliny.autismmap.member.entity.Member;
import com.yaliny.autismmap.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email()).orElseThrow(() -> new MemberNotFoundException("계정이 존재하지 않습니다."));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new InvalidPasswordException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtUtil.generateToken(member.getEmail(), member.getRole().name());
        return new LoginResponse(token);
    }

    @Transactional
    public SignUpResponse signup(SignUpRequest request) {
        if (memberRepository.findByEmail(request.email()).isPresent()) {
            throw new MemberAlreadyExistsException("이미 존재하는 이메일입니다.");
        }

        Member member = new Member(request.email(), passwordEncoder.encode(request.password()), request.nickname());
        memberRepository.save(member);

        String token = jwtUtil.generateToken(member.getEmail(), member.getRole().name());
        return new SignUpResponse(token);
    }
}
