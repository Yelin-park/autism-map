package com.yaliny.autismmap.member.service;

import com.yaliny.autismmap.global.exception.CustomException;
import com.yaliny.autismmap.global.external.kakao.KakaoUnlinkService;
import com.yaliny.autismmap.global.jwt.JwtUtil;
import com.yaliny.autismmap.global.utils.SecurityUtil;
import com.yaliny.autismmap.member.dto.request.LoginRequest;
import com.yaliny.autismmap.member.dto.request.PasswordRequest;
import com.yaliny.autismmap.member.dto.request.SignUpRequest;
import com.yaliny.autismmap.member.dto.response.LoginResponse;
import com.yaliny.autismmap.member.dto.response.MemberInfoResponse;
import com.yaliny.autismmap.member.dto.response.SignUpResponse;
import com.yaliny.autismmap.member.entity.Member;
import com.yaliny.autismmap.member.entity.Provider;
import com.yaliny.autismmap.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.yaliny.autismmap.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final KakaoUnlinkService kakaoUnlinkService;

    public LoginResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email()).orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
        if (!passwordEncoder.matches(request.password(), member.getPassword()))
            throw new CustomException(INVALID_PASSWORD);
        String token = jwtUtil.generateToken(member.getId(), member.getEmail(), member.getRole().name());
        return new LoginResponse(token);
    }

    @Transactional
    public SignUpResponse signup(SignUpRequest request) {
        if (memberRepository.findByEmail(request.email()).isPresent()) throw new CustomException(MEMBER_ALREADY_EXISTS);

        Member member = Member.createMember(request.email(), passwordEncoder.encode(request.password()), request.nickname());
        memberRepository.save(member);

        String token = jwtUtil.generateToken(member.getId(), member.getEmail(), member.getRole().name());
        return new SignUpResponse(token);
    }

    @Transactional
    public void withdraw(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
        Long tokenMemberId = SecurityUtil.getCurrentMemberId();
        if (!memberId.equals(tokenMemberId)) throw new CustomException(ACCESS_DENIED);
        if (member.getProvider() == Provider.KAKAO) {
            kakaoUnlinkService.unlink(member);
        }

        memberRepository.softDeleteByMemberId(member.getId());
    }

    public MemberInfoResponse getMemberInfo(Long memberId) {
        Long tokenMemberId = SecurityUtil.getCurrentMemberId();
        if (!memberId.equals(tokenMemberId)) throw new CustomException(ACCESS_DENIED);
        Member findMember = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
        return MemberInfoResponse.of(findMember);
    }

    @Transactional
    public MemberInfoResponse updateNickname(Long memberId, String nickname) {
        Long tokenMemberId = SecurityUtil.getCurrentMemberId();
        if (!memberId.equals(tokenMemberId)) throw new CustomException(ACCESS_DENIED);
        Member findMember = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
        findMember.updateNickname(nickname);
        return MemberInfoResponse.of(findMember);
    }

    @Transactional
    public void memberDelete(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email()).orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
        if (!passwordEncoder.matches(request.password(), member.getPassword()))
            throw new CustomException(INVALID_PASSWORD);

        if (member.getProvider() == Provider.KAKAO) {
            kakaoUnlinkService.unlink(member);
        }

        memberRepository.softDeleteByMemberId(member.getId());
    }

    @Transactional
    public MemberInfoResponse updatePassword(Long memberId, PasswordRequest request) {
        Long tokenMemberId = SecurityUtil.getCurrentMemberId();
        if (!memberId.equals(tokenMemberId)) throw new CustomException(ACCESS_DENIED);
        Member findMember = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
        if (findMember.isSocial()) {
            throw new CustomException(SOCIAL_ACCOUNT_PASSWORD_CHANGE_NOT_ALLOWED);
        }
        findMember.updatePassword(passwordEncoder.encode(request.password()));
        return MemberInfoResponse.of(findMember);
    }
}
