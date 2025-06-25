package com.yaliny.autismmap.member.service;

import com.yaliny.autismmap.global.exception.InvalidPasswordException;
import com.yaliny.autismmap.global.exception.MemberAlreadyExistsException;
import com.yaliny.autismmap.global.exception.MemberNotFoundException;
import com.yaliny.autismmap.global.exception.NoPermissionException;
import com.yaliny.autismmap.global.jwt.JwtUtil;
import com.yaliny.autismmap.member.dto.request.LoginRequest;
import com.yaliny.autismmap.member.dto.request.SignUpRequest;
import com.yaliny.autismmap.member.dto.response.LoginResponse;
import com.yaliny.autismmap.member.dto.response.MemberInfoResponse;
import com.yaliny.autismmap.member.dto.response.SignUpResponse;
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
        Member member = memberRepository.findByEmail(request.email()).orElseThrow(MemberNotFoundException::new);
        if (!passwordEncoder.matches(request.password(), member.getPassword())) throw new InvalidPasswordException();
        String token = jwtUtil.generateToken(member.getId(), member.getEmail(), member.getRole().name());
        return new LoginResponse(token);
    }

    @Transactional
    public SignUpResponse signup(SignUpRequest request) {
        if (memberRepository.findByEmail(request.email()).isPresent()) throw new MemberAlreadyExistsException();

        Member member = new Member(request.email(), passwordEncoder.encode(request.password()), request.nickname());
        memberRepository.save(member);

        String token = jwtUtil.generateToken(member.getId(), member.getEmail(), member.getRole().name());
        return new SignUpResponse(token);
    }

    @Transactional
    public void withdraw(Long memberId, Long tokenMemberId) {
        if (!tokenMemberId.equals(memberId)) throw new NoPermissionException();

        Member member = memberRepository.findById(memberId)
            .orElseThrow(MemberNotFoundException::new);

        memberRepository.delete(member);
    }

    @Transactional(readOnly = true)
    public MemberInfoResponse getMemberInfo(Long memberId, Long tokenMemberId) {
        if (!tokenMemberId.equals(memberId)) throw new NoPermissionException();
        Member findMember = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        return MemberInfoResponse.of(findMember);
    }
}
