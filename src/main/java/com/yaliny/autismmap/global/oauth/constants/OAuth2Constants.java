package com.yaliny.autismmap.global.oauth.constants;

/**
 * OAuth2 인증 관련 공통 상수
 */
public final class OAuth2Constants {

    // Device 관련
    public static final String DEVICE_PARAM = "device";
    public static final String DEVICE_WEB = "web";
    public static final String DEVICE_APP = "app";

    // State 관련
    public static final String STATE_DELIMITER = "|";
    public static final String DEVICE_PREFIX = "device=";

    // 세션 관련
    public static final String SESSION_DEVICE_KEY = "OAUTH2_DEVICE";

    private OAuth2Constants() {
        throw new AssertionError("상수 클래스는 인스턴스화할 수 없습니다.");
    }
}
