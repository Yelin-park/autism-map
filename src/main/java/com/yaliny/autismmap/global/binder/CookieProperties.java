package com.yaliny.autismmap.global.binder;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "security.cookie")
public class CookieProperties {
    private String sameSite;   // Lax, None, Strict
    private boolean secure;
    private String domain;
    private long maxAge;
}
