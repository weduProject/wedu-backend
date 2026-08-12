package com.wedu.global.security.oauth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

/**
 * OAuth2 authorization request 를 HTTP 세션 대신 쿠키에 둔다.
 *
 * <p>JWT 기반 STATELESS 정책과 함께 쓰기 위함이다. 로그인 콜백이 끝나면 쿠키를 지운다.
 * 쿠키 값은 Java 직렬화 대신 JSON 으로 필요한 필드만 저장한다.
 *
 * <p>로그인 시작 시 {@code redirect_uri} 쿼리로 받은 프론트 콜백은 allowlist 검증 후
 * {@link #FRONTEND_REDIRECT_COOKIE_NAME} 쿠키에 따로 보관한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTH_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    public static final String FRONTEND_REDIRECT_COOKIE_NAME = "oauth2_frontend_redirect";
    private static final int COOKIE_EXPIRE_SECONDS = 180;
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;
    private final FrontendRedirectUriResolver frontendRedirectUriResolver;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return readAuthorizationRequestCookie(request);
    }

    @Override
    public void saveAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        if (authorizationRequest == null) {
            removeAuthorizationRequestCookies(request, response);
            return;
        }
        writeCookie(
                request,
                response,
                OAUTH2_AUTH_REQUEST_COOKIE_NAME,
                serialize(authorizationRequest));
        saveFrontendRedirectCookie(request, response);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
            HttpServletRequest request, HttpServletResponse response) {
        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        removeAuthorizationRequestCookies(request, response);
        return authorizationRequest;
    }

    public String loadFrontendRedirectUri(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, FRONTEND_REDIRECT_COOKIE_NAME);
        if (cookie == null || !StringUtils.hasText(cookie.getValue())) {
            return null;
        }
        try {
            return new String(
                    Base64.getUrlDecoder().decode(cookie.getValue()), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            log.debug("Failed to decode frontend redirect cookie: {}", ex.getMessage());
            return null;
        }
    }

    public void removeAuthorizationRequestCookies(
            HttpServletRequest request, HttpServletResponse response) {
        deleteCookie(request, response, OAUTH2_AUTH_REQUEST_COOKIE_NAME);
        deleteCookie(request, response, FRONTEND_REDIRECT_COOKIE_NAME);
    }

    private void saveFrontendRedirectCookie(
            HttpServletRequest request, HttpServletResponse response) {
        String candidate = request.getParameter(FrontendRedirectUriResolver.REDIRECT_URI_PARAM);
        frontendRedirectUriResolver
                .resolve(candidate)
                .ifPresentOrElse(
                        uri -> writeCookie(
                                request,
                                response,
                                FRONTEND_REDIRECT_COOKIE_NAME,
                                Base64.getUrlEncoder()
                                        .withoutPadding()
                                        .encodeToString(uri.getBytes(StandardCharsets.UTF_8))),
                        () -> deleteCookie(request, response, FRONTEND_REDIRECT_COOKIE_NAME));
    }

    private OAuth2AuthorizationRequest readAuthorizationRequestCookie(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, OAUTH2_AUTH_REQUEST_COOKIE_NAME);
        if (cookie == null || !StringUtils.hasText(cookie.getValue())) {
            return null;
        }
        return deserialize(cookie.getValue());
    }

    private void writeCookie(
            HttpServletRequest request, HttpServletResponse response, String name, String value) {
        Cookie cookie = new Cookie(name, value);
        applyCookieSecurity(cookie, request);
        cookie.setMaxAge(COOKIE_EXPIRE_SECONDS);
        response.addCookie(cookie);
    }

    private void deleteCookie(
            HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        applyCookieSecurity(cookie, request);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private void applyCookieSecurity(Cookie cookie, HttpServletRequest request) {
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setAttribute("SameSite", "Lax");
    }

    private String serialize(OAuth2AuthorizationRequest authorizationRequest) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("authorizationUri", authorizationRequest.getAuthorizationUri());
            payload.put("clientId", authorizationRequest.getClientId());
            payload.put("redirectUri", authorizationRequest.getRedirectUri());
            payload.put("scopes", authorizationRequest.getScopes());
            payload.put("state", authorizationRequest.getState());
            payload.put("additionalParameters", authorizationRequest.getAdditionalParameters());
            payload.put("attributes", authorizationRequest.getAttributes());
            payload.put("authorizationRequestUri", authorizationRequest.getAuthorizationRequestUri());
            return Base64.getUrlEncoder().encodeToString(objectMapper.writeValueAsBytes(payload));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize OAuth2AuthorizationRequest", e);
        }
    }

    private OAuth2AuthorizationRequest deserialize(String value) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(value);
            Map<String, Object> payload = objectMapper.readValue(bytes, MAP_TYPE);
            String authorizationUri = stringValue(payload.get("authorizationUri"));
            String clientId = stringValue(payload.get("clientId"));
            String redirectUri = stringValue(payload.get("redirectUri"));
            String state = stringValue(payload.get("state"));
            String authorizationRequestUri = stringValue(payload.get("authorizationRequestUri"));
            if (!StringUtils.hasText(authorizationUri) || !StringUtils.hasText(clientId)) {
                return null;
            }
            return OAuth2AuthorizationRequest.authorizationCode()
                    .authorizationUri(authorizationUri)
                    .clientId(clientId)
                    .redirectUri(redirectUri)
                    .scopes(toStringSet(payload.get("scopes")))
                    .state(state)
                    .additionalParameters(toStringObjectMap(payload.get("additionalParameters")))
                    .attributes(toStringObjectMap(payload.get("attributes")))
                    .authorizationRequestUri(authorizationRequestUri)
                    .build();
        } catch (Exception e) {
            log.debug("Failed to deserialize OAuth2AuthorizationRequest cookie: {}", e.getMessage());
            return null;
        }
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Set<String> toStringSet(Object value) {
        if (!(value instanceof Collection<?> collection)) {
            return Set.of();
        }
        Set<String> scopes = new HashSet<>();
        for (Object item : collection) {
            if (item != null) {
                scopes.add(String.valueOf(item));
            }
        }
        return scopes;
    }

    private Map<String, Object> toStringObjectMap(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            return Map.of();
        }
        Map<String, Object> result = new HashMap<>();
        map.forEach((key, entryValue) -> {
            if (key != null) {
                result.put(String.valueOf(key), entryValue);
            }
        });
        return result;
    }
}
