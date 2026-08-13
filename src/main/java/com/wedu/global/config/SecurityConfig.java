package com.wedu.global.config;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import com.wedu.global.security.jwt.JwtAuthenticationFilter;
import com.wedu.global.security.jwt.JwtTokenProvider;
import com.wedu.global.security.oauth.HttpCookieOAuth2AuthorizationRequestRepository;
import com.wedu.global.security.oauth.NoopOAuth2AuthorizedClientRepository;
import com.wedu.global.security.oauth.OAuth2FailureHandler;
import com.wedu.global.security.oauth.OAuth2SuccessHandler;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * 애플리케이션 보안 정책.
 *
 * <p>세션 없는(stateless) JWT 인증. 소셜 로그인은 OAuth2 authorization code + 쿠키 기반
 * authorization request 저장으로 처리하고, 성공 시 JWT 를 발급한다.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * 인증 없이 접근 가능한, 메서드 무관 경로.
     *
     * <p>같은 prefix 에 쓰기 API 가 추가돼도 실수로 공개되지 않도록, 인증/상품 조회는
     * {@link #PUBLIC_AUTH_ENDPOINTS}/{@link #PUBLIC_PRODUCT_GET_ENDPOINTS} 로 경로·메서드를 좁혀서 둔다.
     */
    private static final String[] PUBLIC_ENDPOINTS = {
            "/oauth2/**",
            "/login/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/actuator/health",
            "/api/ddays/shared/**",
            "/api/calendar-events/shared/**",
            "/api/checklist-items/shared/**",
            "/api/budget-items/shared/**",
    };

    /** 인증 없이 호출 가능한 인증(회원가입/로그인) API. */
    private static final String[] PUBLIC_AUTH_ENDPOINTS = {
            "/api/auth/signup",
            "/api/auth/login",
            "/api/auth/oauth/token",
            "/api/auth/temp-login",
    };

    /**
     * 인증 없이 GET 만 허용하는 경로.
     *
     * <p>상품 목록/상세/인기 상품 API와, {@code src/main/resources/static/products/} 의 상품
     * 썸네일 정적 파일(Spring Boot 기본 정적 리소스 서빙 경로 {@code /products/**}) 둘 다 포함한다.
     */
    private static final String[] PUBLIC_PRODUCT_GET_ENDPOINTS = {
            "/api/products",
            "/api/products/*",
            "/products/**",
    };

    private final JwtTokenProvider jwtTokenProvider;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;

    @Value("${wedu.cors.allowed-origins:http://localhost:3000}")
    private List<String> allowedOrigins;

    @Bean
    @Primary
    public OAuth2AuthorizedClientRepository oauth2AuthorizedClientRepository() {
        return new NoopOAuth2AuthorizedClientRepository();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            OAuth2AuthorizedClientRepository oauth2AuthorizedClientRepository)
            throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.POST, PUBLIC_AUTH_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_PRODUCT_GET_ENDPOINTS).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exception ->
                        exception.defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(UNAUTHORIZED),
                                new AntPathRequestMatcher("/api/**")))
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(endpoint -> endpoint
                                .authorizationRequestRepository(
                                        authorizationRequestRepository))
                        .authorizedClientRepository(
                                oauth2AuthorizedClientRepository)
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler))
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 공유 dev 서버가 없어 팀원들이 로컬 개발 중에도 이 서버(prod)를 직접 호출하는 상황을
        // 감안해, 운영 도메인(allowedOrigins)에 로컬 개발 서버 origin을 추가로 항상 허용한다.
        List<String> corsOrigins = new ArrayList<>(allowedOrigins);
        corsOrigins.addAll(LocalDevOrigins.ORIGINS);
        configuration.setAllowedOrigins(corsOrigins);
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(
                List.of("Authorization", "Content-Type", "Accept", "Origin"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
