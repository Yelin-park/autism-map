package com.yaliny.autismmap.member.oauth;

import com.yaliny.autismmap.member.entity.Provider;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String providerName, Map<String, Object> attributes) {
        return switch (providerName.toUpperCase()) {
            case "GOOGLE" -> new GoogleUserInfo(attributes);
            case "KAKAO" -> new KakaoUserInfo(attributes);
            //case "NAVER" -> new NaverUserInfo(attributes);
            default -> throw new IllegalArgumentException("Unknown provider: " + providerName);
        };
    }
}
