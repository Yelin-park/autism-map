package com.yaliny.autismmap.global.external.kakao;

import com.fasterxml.jackson.databind.JsonNode;
import com.yaliny.autismmap.member.oauth.KakaoUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    @Value("${oauth2.kakao.admin-key}")
    private String adminKey;

    private final String tokenUri = "https://kauth.kakao.com/oauth/token";
    private final String userInfoUri = "https://kapi.kakao.com/v2/user/me";
    private final String unlinkUri = "https://kapi.kakao.com/v1/user/unlink";

    public String getAccessToken(String code) {
        String form =
            "grant_type=authorization_code" +
                "&client_id=" + urlEncode(clientId) +
                "&redirect_uri=" + urlEncode(redirectUri) +
                "&code=" + urlEncode(code);

        return webClient.post()
            .uri(tokenUri)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .bodyValue(form)
            .retrieve()
            .bodyToMono(JsonNode.class)
            .doOnNext(body -> log.info("카카오 access_token 응답: {}", body))
            .map(json -> json.get("access_token").asText())
            .block(); // ⚠️ 비동기 처리 원할 시 .block() 제거하고 Mono 리턴
    }

    public KakaoUserInfo getUserInfo(String accessToken) {
        Map attributes = webClient.get()
            .uri(userInfoUri)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .retrieve()
            .bodyToMono(Map.class)
            .doOnNext(body -> log.info("카카오 사용자 정보 응답: {}", body))
            .block();

        return new KakaoUserInfo(attributes);
    }

    /**
     * Admin Key + target_id 방식으로 연결 해제
     */
    public Long unlinkByAdminKey(long kakaoUserId) {
        if (adminKey == null || adminKey.isBlank()) {
            throw new IllegalStateException("Kakao Admin Key is not configured.");
        }
        String form = "target_id_type=user_id&target_id=" + kakaoUserId;

        Map res = webClient.post()
            .uri(unlinkUri)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + adminKey)
            .bodyValue(form)
            .retrieve()
            .bodyToMono(Map.class)
            .doOnNext(body -> log.info("카카오 unlink(AdminKey) 응답: {}", body))
            .block();

        return extractId(res);
    }

    /**
     * 응답에서 id 추출
     */
    private Long extractId(Map<String, Object> res) {
        if (res == null || !res.containsKey("id")) {
            throw new IllegalStateException("Kakao unlink response missing id");
        }
        Object id = res.get("id");
        if (id instanceof Number n) return n.longValue();
        return Long.parseLong(String.valueOf(id));
    }

    private String urlEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
