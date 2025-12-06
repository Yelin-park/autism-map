package com.yaliny.autismmap.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaliny.autismmap.global.exception.ErrorCode;
import com.yaliny.autismmap.global.jwt.JwtFilter;
import com.yaliny.autismmap.global.oauth.CustomOAuth2AuthorizationRequestRepository;
import com.yaliny.autismmap.global.oauth.CustomOAuth2AuthorizationRequestResolver;
import com.yaliny.autismmap.global.response.BaseResponse;
import com.yaliny.autismmap.member.handler.CustomOAuth2FailureHandler;
import com.yaliny.autismmap.member.handler.CustomOAuth2SuccessHandler;
import com.yaliny.autismmap.member.service.CustomOAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final ObjectMapper objectMapper;
    private final CustomOAuthService customOAuthService;
    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
    private final CustomOAuth2FailureHandler customOAuth2FailureHandler;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public CustomOAuth2AuthorizationRequestRepository customAuthRequestRepository() {
        return new CustomOAuth2AuthorizationRequestRepository();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .accessDeniedHandler(accessDeniedHandler()) // 커스텀 핸들러 사용
                .defaultAuthenticationEntryPointFor(
                    restAuthenticationEntryPoint,
                    new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/api/**")
                )
            )
            // H2 Console은 iframe 기반 → 기본 Security 설정에서는 iframe 금지 → 403 발생 → disable() 설정으로 해결
            .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
            .authorizeHttpRequests(auth -> auth
                // Actuator health, info만 공개
                .requestMatchers(EndpointRequest.to(HealthEndpoint.class, InfoEndpoint.class)).permitAll()
                .requestMatchers(
                    "/",
                    "/h2-console/**",
                    "/swagger-ui/**",
                    "/swagger-resources/**",
                    "/v3/api-docs/**",
                    "/favicon.ico",
                    "/nurean/v1/api-docs/**",
                    "/api/v1/members/signup",
                    "/api/v1/members/login",
                    "/api/v1/members/logout",
                    "/api/v1/regions/**",
                    "/api/v1/oauth/**",
                    "/oauth/**",
                    "/oauth2/**",
                    "/login/oauth2/**",
                    "/oauth2/authorization/**"
                ).permitAll() // 인증없이 허용
                .requestMatchers(HttpMethod.GET, "/api/v1/places").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/places/{placeId}").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/v1/members").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/places").hasRole("ADMIN")            // ADMIN 권한만 접근 허용
                .requestMatchers(HttpMethod.PATCH, "/api/v1/places/{placeId}").hasRole("ADMIN") // ADMIN 권한만 접근 허용
                .requestMatchers(HttpMethod.DELETE, "/api/v1/places/{placeId}").hasRole("ADMIN") // ADMIN 권한만 접근 허용
                .anyRequest().authenticated() // 나머지 요청은 인증 필요
            )
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(authorization ->
                    authorization.authorizationRequestResolver(
                            new CustomOAuth2AuthorizationRequestResolver(clientRegistrationRepository)
                        )
                        .authorizationRequestRepository(customAuthRequestRepository())
                )
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuthService)) // 사용자 정보 처리
                .successHandler(customOAuth2SuccessHandler) // JWT 발급 후 리디렉션
                .failureHandler(customOAuth2FailureHandler)
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 패스워드 암호화에 사용할 PasswordEncoder 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager 등록 (로그인 시 사용 가능)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json;charset=UTF-8");

            BaseResponse<Void> baseResponse = BaseResponse.error(
                ErrorCode.ACCESS_DENIED.getStatus().value(),
                ErrorCode.ACCESS_DENIED.getMessage()
            );

            response.getWriter().write(objectMapper.writeValueAsString(baseResponse));
        };
    }

}
