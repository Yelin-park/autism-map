package com.yaliny.autismmap.global.external.kakao;

import com.fasterxml.jackson.databind.JsonNode;
import com.yaliny.autismmap.global.exception.CustomException;
import com.yaliny.autismmap.global.exception.ErrorCode;
import com.yaliny.autismmap.member.oauth.KakaoUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 카카오 OAuth2 클라이언트
 * - 액세스 토큰 발급
 * - 사용자 정보 조회
 * - 연결 해제 (Admin Key 방식)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KakaoOAuthClient {

    private static final String TOKEN_URI = "https://kauth.kakao.com/oauth/token";
    private static final String USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";
    private static final String UNLINK_URI = "https://kapi.kakao.com/v1/user/unlink";

    private final WebClient webClient;

    @Value("${oauth2.kakao.client-id}")
    private String clientId;

    @Value("${oauth2.kakao.redirect-uri}")
    private String redirectUri;

    @Value("${oauth2.kakao.admin-key}")
    private String adminKey;

    public KakaoOAuthClient() {
        this.webClient = WebClient.builder()
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .build();
    }

    /**
     * 카카오 액세스 토큰 발급 (동기 방식)
     *
     * @param code 인가 코드
     * @return 액세스 토큰
     * @throws CustomException 토큰 발급 실패 시
     */
    public String getAccessToken(String code) {
        try {
            String form = buildTokenRequestForm(code);

            return webClient.post()
                .uri(TOKEN_URI)
                .bodyValue(form)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleTokenError)
                .bodyToMono(JsonNode.class)
                .doOnNext(body -> log.debug("[KakaoOAuthClient] 액세스 토큰 응답: {}", body))
                .map(json -> json.get("access_token").asText())
                .block();  // 비동기 처리 원할 시 .block() 제거하고 Mono 리턴

        } catch (Exception e) {
            log.error("[KakaoOAuthClient] 액세스 토큰 발급 실패", e);
            throw new CustomException(ErrorCode.OAUTH_TOKEN_REQUEST_FAILED);
        }
    }

    /**
     * 카카오 액세스 토큰 발급 (비동기 방식)
     *
     * @param code 인가 코드
     * @return 액세스 토큰 Mono
     */
    public Mono<String> getAccessTokenAsync(String code) {
        String form = buildTokenRequestForm(code);

        return webClient.post()
            .uri(TOKEN_URI)
            .bodyValue(form)
            .retrieve()
            .onStatus(HttpStatusCode::isError, this::handleTokenError)
            .bodyToMono(JsonNode.class)
            .doOnNext(body -> log.debug("[KakaoOAuthClient] 액세스 토큰 응답: {}", body))
            .map(json -> json.get("access_token").asText())
            .doOnError(e -> log.error("[KakaoOAuthClient] 액세스 토큰 발급 실패", e));
    }

    /**
     * 카카오 사용자 정보 조회 (동기 방식)
     *
     * @param accessToken 액세스 토큰
     * @return 카카오 사용자 정보
     * @throws CustomException 사용자 정보 조회 실패 시
     */
    public KakaoUserInfo getUserInfo(String accessToken) {
        try {
            Map<String, Object> attributes = webClient.get()
                .uri(USER_INFO_URI)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleUserInfoError)
                .bodyToMono(Map.class)
                .doOnNext(body -> log.info("[KakaoOAuthClient] 사용자 정보 응답: {}", body))
                .block();

            return new KakaoUserInfo(attributes);

        } catch (Exception e) {
            log.error("[KakaoOAuthClient] 사용자 정보 조회 실패", e);
            throw new CustomException(ErrorCode.OAUTH_USER_INFO_REQUEST_FAILED);
        }
    }

    /**
     * Admin Key를 사용한 카카오 연결 해제
     *
     * @param kakaoUserId 카카오 사용자 ID
     * @return 연결 해제된 사용자 ID
     * @throws IllegalStateException Admin Key 미설정 시
     * @throws CustomException       연결 해제 실패 시
     */
    public Long unlinkByAdminKey(long kakaoUserId) {
        validateAdminKey();

        try {
            String form = "target_id_type=user_id&target_id=" + kakaoUserId;

            Map<String, Object> response = webClient.post()
                .uri(UNLINK_URI)
                .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + adminKey)
                .bodyValue(form)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleUnlinkError)
                .bodyToMono(Map.class)
                .doOnNext(body -> log.debug("[KakaoOAuthClient] 연결 해제 응답: {}", body))
                .block();

            return extractIdFromResponse(response);

        } catch (Exception e) {
            log.error("[KakaoOAuthClient] 카카오 연결 해제 실패. userId: {}", kakaoUserId, e);
            throw new CustomException(ErrorCode.OAUTH_UNLINK_FAILED);
        }
    }

    /**
     * 토큰 요청 폼 데이터 생성
     */
    private String buildTokenRequestForm(String code) {
        return "grant_type=authorization_code" +
            "&client_id=" + urlEncode(clientId) +
            "&redirect_uri=" + urlEncode(redirectUri) +
            "&code=" + urlEncode(code);
    }

    /**
     * Admin Key 검증
     */
    private void validateAdminKey() {
        if (adminKey == null || adminKey.isBlank()) {
            log.error("[KakaoOAuthClient] Admin Key가 설정되지 않았습니다.");
            throw new IllegalStateException("Kakao Admin Key is not configured.");
        }
    }

    /**
     * 응답에서 ID 추출
     */
    private Long extractIdFromResponse(Map<String, Object> response) {
        if (response == null || !response.containsKey("id")) {
            log.error("[KakaoOAuthClient] 응답에 id 필드가 없습니다. response: {}", response);
            throw new IllegalStateException("Kakao unlink response missing id");
        }

        Object id = response.get("id");
        if (id instanceof Number number) {
            return number.longValue();
        }

        return Long.parseLong(String.valueOf(id));
    }

    /**
     * 토큰 발급 에러 처리
     */
    private Mono<? extends Throwable> handleTokenError(
        org.springframework.web.reactive.function.client.ClientResponse response
    ) {
        return response.bodyToMono(String.class)
            .flatMap(errorBody -> {
                log.error("[KakaoOAuthClient] 토큰 발급 실패. status: {}, body: {}",
                    response.statusCode(), errorBody);
                return Mono.error(new CustomException(ErrorCode.OAUTH_TOKEN_REQUEST_FAILED));
            });
    }

    /**
     * 사용자 정보 조회 에러 처리
     */
    private Mono<? extends Throwable> handleUserInfoError(
        org.springframework.web.reactive.function.client.ClientResponse response
    ) {
        return response.bodyToMono(String.class)
            .flatMap(errorBody -> {
                log.error("[KakaoOAuthClient] 사용자 정보 조회 실패. status: {}, body: {}",
                    response.statusCode(), errorBody);
                return Mono.error(new CustomException(ErrorCode.OAUTH_USER_INFO_REQUEST_FAILED));
            });
    }

    /**
     * 연결 해제 에러 처리
     */
    private Mono<? extends Throwable> handleUnlinkError(
        org.springframework.web.reactive.function.client.ClientResponse response
    ) {
        return response.bodyToMono(String.class)
            .flatMap(errorBody -> {
                log.error("[KakaoOAuthClient] 연결 해제 실패. status: {}, body: {}",
                    response.statusCode(), errorBody);
                return Mono.error(new CustomException(ErrorCode.OAUTH_UNLINK_FAILED));
            });
    }

    /**
     * URL 인코딩
     */
    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
