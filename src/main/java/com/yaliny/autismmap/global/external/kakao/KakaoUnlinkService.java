package com.yaliny.autismmap.global.external.kakao;

import com.yaliny.autismmap.global.exception.CustomException;
import com.yaliny.autismmap.global.exception.ErrorCode;
import com.yaliny.autismmap.member.entity.Member;
import com.yaliny.autismmap.member.entity.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoUnlinkService {

    private final KakaoOAuthClient kakaoOAuthClient;

    /**
     * 카카오 사용자 연결 해제
     */
    public void unlink(Member member) {
        if (member.getProvider() != Provider.KAKAO) return;
        log.info("Unlink Kakao OAuth Client");
        Long expected = parseProviderId(member.getProviderId());
        Long unlinkedId = kakaoOAuthClient.unlinkByAdminKey(expected);

        if (!expected.equals(unlinkedId)) {
            throw new CustomException(ErrorCode.KAKAO_UNLINK_ID_MISMATCH);
        }
    }

    private Long parseProviderId(String providerId) {
        try {
            return Long.valueOf(providerId);
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.INVALID_PROVIDER_ID_FORMAT);
        }
    }

}
