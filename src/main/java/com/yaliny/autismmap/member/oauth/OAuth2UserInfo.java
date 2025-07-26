package com.yaliny.autismmap.member.oauth;

import com.yaliny.autismmap.member.entity.Provider;

public interface OAuth2UserInfo {
    Provider getProvider();     // ex. "GOOGLE"
    String getProviderId();   // ex. sub, id
    String getEmail();
    String getName();         // 닉네임 등
}
