package com.yaliny.autismmap.member.service;

import com.yaliny.autismmap.global.external.kakao.KakaoOAuthClient;
import com.yaliny.autismmap.global.jwt.JwtUtil;
import com.yaliny.autismmap.member.entity.Member;
import com.yaliny.autismmap.member.entity.Provider;
import com.yaliny.autismmap.member.oauth.OAuth2UserInfo;
import com.yaliny.autismmap.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static com.yaliny.autismmap.global.exception.ErrorCode.*;
import static com.yaliny.autismmap.member.oauth.OAuth2UserInfoFactory.getOAuth2UserInfo;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuthService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegateOAuth2UserService;
    private final MemberRepository memberRepository;
    private final KakaoOAuthClient kakaoOAuthClient;
    private final JwtUtil jwtUtil;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = delegateOAuth2UserService.loadUser(request);
        String providerName = request.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        OAuth2UserInfo oAuth2UserInfo = getOAuth2UserInfo(providerName, attributes);
        Provider provider = oAuth2UserInfo.getProvider();
        String providerId = oAuth2UserInfo.getProviderId();
        String email = oAuth2UserInfo.getEmail();
        String nickname = oAuth2UserInfo.getName();

        // 이메일과 닉네임으로 기존 회원 확인 후 Member 저장
        Member member = getOrCreateSocialMember(email, provider, providerId, nickname);

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

    @Transactional
    public String kakaoLogin(String code) {
        // 1. 인가 코드 → 액세스 토큰
        String accessToken = kakaoOAuthClient.getAccessToken(code);

        // 2. 사용자 정보 요청
        OAuth2UserInfo userInfo = kakaoOAuthClient.getUserInfo(accessToken);
        String email = userInfo.getEmail();
        String providerId = userInfo.getProviderId();
        String nickname = userInfo.getName();
        Provider provider = userInfo.getProvider();

        // 3. 회원 조회 or 가입
        Member member = getOrCreateSocialMember(email, provider, providerId, nickname);

        // 4. JWT 발급
        return jwtUtil.generateToken(member.getId(), member.getEmail(), member.getRole().name());
    }


    @Transactional
    protected Member getOrCreateSocialMember(String email, Provider provider, String providerId, String baseName) throws InternalAuthenticationServiceException {
        // 이메일 조회
        Member member = memberRepository.findByEmail(email).map(existing -> {
            if (!existing.isSocial()) {
                throw new InternalAuthenticationServiceException(MEMBER_ALREADY_EXISTS.getMessage());
            }
            if (!provider.equals(existing.getProvider())) {
                throw new InternalAuthenticationServiceException(DUPLICATE_SOCIAL_EMAIL.getMessage());
            }
            return existing;
        }).orElseGet(() -> {
            String nickname = generateUniqueNickname(baseName);

            for (int i = 0; i < 5; i++) {
                try {
                    Member newMember = Member.socialSignup(email, nickname, provider, providerId);
                    return memberRepository.save(newMember);
                } catch (DataIntegrityViolationException ex) { // 닉네임 충돌 시 닉네임만 다시 생성 후 재시도
                    if (isEmailDuplicate(ex)) {
                        throw new InternalAuthenticationServiceException(MEMBER_ALREADY_EXISTS.getMessage());
                    }

                    nickname = generateUniqueNickname(baseName);
                }
            }

            throw new InternalAuthenticationServiceException(NICKNAME_CREATE_COLLISION.getMessage());
        });

        return member;
    }

    private String generateUniqueNickname(String preferred) {
        // 전처리/정규화 (공백 제거, 길이 제한 등)
        String base = normalize(preferred);
        if (base.isBlank()) base = "사용자";

        // 빠른 후보 + 재시도(최대 10회)
        for (int i = 0; i < 10; i++) {
            String candidate = base + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
            if (!memberRepository.existsByNickname(candidate)) {
                return candidate; // 저장 직전에 또 경쟁이 생길 수 있어도 우선 반환
            }
        }
        // 폴백(거의 확실한 유니크로 UUID 사용)
        return base + "-" + UUID.randomUUID().toString().substring(0, 6);
    }

    private String normalize(String s) {
        if (s == null) return "";
        String trimmed = s.trim().replaceAll("\\s+", "");
        return trimmed.length() > 16 ? trimmed.substring(0, 16) : trimmed;
    }

    private boolean isEmailDuplicate(DataIntegrityViolationException e) {
        return e.getMessage().contains("email");
    }
}
