package com.yaliny.autismmap.global.external.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.yaliny.autismmap.member.oauth.KakaoUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class KakaoOAuthClient {
    private final WebClient webClient = WebClient.builder().build();

    @Value("${oauth2.kakao.client-id}")
    private String clientId;

    @Value("${oauth2.kakao.redirect-uri}")
    private String redirectUri;

    private final String tokenUri = "https://kauth.kakao.com/oauth/token";
    private final String userInfoUri = "https://kapi.kakao.com/v2/user/me";

    public String getAccessToken(String code) {
        return webClient.post()
            .uri(tokenUri)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .bodyValue("grant_type=authorization_code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&code=" + code)
            .retrieve()
            .bodyToMono(JsonNode.class)
            .doOnNext(body -> log.info("카카오 access_token 응답: {}", body))
            .map(json -> json.get("access_token").asText())
            .block(); // ⚠️ 비동기 처리 원할 시 .block() 제거하고 Mono 리턴
    }

    public KakaoUserInfo getUserInfo(String accessToken) {
        Map<String, Object> attributes = webClient.get()
            .uri(userInfoUri)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .retrieve()
            .bodyToMono(Map.class)
            .doOnNext(body -> log.info("카카오 사용자 정보 응답: {}", body))
            .block();

        return new KakaoUserInfo(attributes);
    }
}
