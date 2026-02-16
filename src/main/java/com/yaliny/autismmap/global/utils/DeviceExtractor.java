package com.yaliny.autismmap.global.utils;

import com.yaliny.autismmap.global.oauth.constants.OAuth2Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * OAuth2 state 파라미터에서 device 정보를 추출하는 유틸리티
 */
@Slf4j
@Component
public class DeviceExtractor {

    /**
     * state 파라미터에서 device 값을 추출
     * 형식: "random_state|device=app" 또는 "random_state|device=web"
     *
     * @param state OAuth2 state 파라미터
     * @return device 값 (app 또는 web), 없으면 기본값 web 반환
     */
    public String extractFromState(String state) {
        if (state == null || state.isBlank()) {
            log.debug("[DeviceExtractor] state가 비어있어 기본값({}) 반환", OAuth2Constants.DEVICE_WEB);
            return OAuth2Constants.DEVICE_WEB;
        }

        String[] parts = state.split("\\Q" + OAuth2Constants.STATE_DELIMITER + "\\E");
        for (String part : parts) {
            if (part != null && part.startsWith(OAuth2Constants.DEVICE_PREFIX)) {
                String device = part.substring(OAuth2Constants.DEVICE_PREFIX.length());
                log.debug("[DeviceExtractor] state에서 device 추출: {}", device);
                return device;
            }
        }

        log.debug("[DeviceExtractor] device를 찾을 수 없어 기본값({}) 반환", OAuth2Constants.DEVICE_WEB);
        return OAuth2Constants.DEVICE_WEB;
    }

    /**
     * device가 앱인지 확인
     *
     * @param device device 값
     * @return 앱이면 true
     */
    public boolean isApp(String device) {
        return OAuth2Constants.DEVICE_APP.equalsIgnoreCase(device);
    }

    /**
     * device가 웹인지 확인
     *
     * @param device device 값
     * @return 웹이면 true
     */
    public boolean isWeb(String device) {
        return device == null || OAuth2Constants.DEVICE_WEB.equalsIgnoreCase(device);
    }
}
