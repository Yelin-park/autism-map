package com.yaliny.autismmap.member.service;

import com.yaliny.autismmap.global.exception.CustomException;
import com.yaliny.autismmap.global.exception.ErrorCode;
import com.yaliny.autismmap.member.entity.Member;
import com.yaliny.autismmap.member.entity.Provider;
import com.yaliny.autismmap.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String provider = request.getClientRegistration().getRegistrationId().toUpperCase();
        String providerId = (String) attributes.get("sub");
        String email = (String) attributes.get("email");
        String nickname = (String) attributes.get("name");

        // 이메일로 기존 회원 확인
        Member member = memberRepository.findByEmail(email).map(existing -> {
            if (!existing.isSocial()) {
                throw new CustomException(ErrorCode.MEMBER_ALREADY_EXISTS); // 일반 회원과 충돌
            }
            if (!provider.equals(existing.getProvider().name())) {
                throw new CustomException(ErrorCode.DUPLICATE_SOCIAL_EMAIL); // 다른 소셜 플랫폼으로 가입된 이메일
            }
            return existing;
        }).orElseGet(() -> {
            Member newMember = Member.socialSignup(email, nickname, Provider.GOOGLE, providerId);
            return memberRepository.save(newMember);
        });

        // OAuth2User 반환 (SecurityContext 저장용)
        return new DefaultOAuth2User(
            List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole())),
            attributes,
            "sub" // 구글의 고유 사용자 식별자 key
        );
    }
}
