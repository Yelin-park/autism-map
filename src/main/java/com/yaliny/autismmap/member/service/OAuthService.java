package com.yaliny.autismmap.member.service;

import com.yaliny.autismmap.global.exception.CustomException;
import com.yaliny.autismmap.global.exception.ErrorCode;
import com.yaliny.autismmap.member.entity.Member;
import com.yaliny.autismmap.member.entity.Provider;
import com.yaliny.autismmap.member.oauth.OAuth2UserInfo;
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

import static com.yaliny.autismmap.member.oauth.OAuth2UserInfoFactory.getOAuth2UserInfo;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);
        String providerName = request.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        OAuth2UserInfo oAuth2UserInfo = getOAuth2UserInfo(providerName, attributes);
        Provider provider = oAuth2UserInfo.getProvider();
        String providerId = oAuth2UserInfo.getProviderId();
        String email = oAuth2UserInfo.getEmail();
        String nickname = oAuth2UserInfo.getName();

        // 이메일로 기존 회원 확인
        Member member = memberRepository.findByEmail(email).map(existing -> {
            if (!existing.isSocial()) {
                throw new CustomException(ErrorCode.MEMBER_ALREADY_EXISTS); // 일반 회원과 충돌
            }
            if (!provider.equals(existing.getProvider())) {
                throw new CustomException(ErrorCode.DUPLICATE_SOCIAL_EMAIL); // 다른 소셜 플랫폼으로 가입된 이메일
            }
            return existing;
        }).orElseGet(() -> {
            Member newMember = Member.socialSignup(email, nickname, provider, providerId);
            return memberRepository.save(newMember);
        });

        // 사용자 식별자 키 동적 처리(구글은 sub, 카카오는 id..등)
        String userNameAttribute = request.getClientRegistration()
            .getProviderDetails()
            .getUserInfoEndpoint()
            .getUserNameAttributeName();

        // OAuth2User 반환 (SecurityContext 저장용)
        return new DefaultOAuth2User(
            List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole())),
            attributes,
            userNameAttribute
        );
    }
}
