package com.wedu.global.security.oauth;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 소셜 로그인 후 프론트 콜백 URL을 allowlist 기준으로 검증한다.
 *
 * <p>로그인 시작 시 {@code redirect_uri} 쿼리로 전달된 값이 허용 목록에 있을 때만 사용하고,
 * 없거나 불허면 {@code wedu.oauth2.frontend-redirect-uri} 기본값을 쓴다.
 */
@Component
public class FrontendRedirectUriResolver {

    public static final String REDIRECT_URI_PARAM = "redirect_uri";
    public static final String CALLBACK_PATH = "/auth/callback";

    private final String defaultFrontendRedirectUri;
    private final Set<String> allowedRedirectUris;

    public FrontendRedirectUriResolver(
            @Value("${wedu.oauth2.frontend-redirect-uri}") String defaultFrontendRedirectUri,
            @Value("${wedu.cors.allowed-origins}") List<String> allowedOrigins) {
        this.defaultFrontendRedirectUri = requireNormalizedCallback(defaultFrontendRedirectUri);
        Set<String> allowed = new LinkedHashSet<>();
        for (String origin : allowedOrigins) {
            normalizeToCallback(origin).ifPresent(allowed::add);
        }
        allowed.add(this.defaultFrontendRedirectUri);
        this.allowedRedirectUris = Set.copyOf(allowed);
    }

    public String resolveOrDefault(String candidate) {
        return resolve(candidate).orElse(defaultFrontendRedirectUri);
    }

    public String getDefaultFrontendRedirectUri() {
        return defaultFrontendRedirectUri;
    }

    public Optional<String> resolve(String candidate) {
        if (!StringUtils.hasText(candidate)) {
            return Optional.empty();
        }
        return normalizeToCallback(candidate.trim())
                .filter(allowedRedirectUris::contains);
    }

    private String requireNormalizedCallback(String value) {
        return normalizeToCallback(value)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid wedu.oauth2.frontend-redirect-uri: " + value));
    }

    private Optional<String> normalizeToCallback(String raw) {
        if (!StringUtils.hasText(raw)) {
            return Optional.empty();
        }
        try {
            URI uri = URI.create(raw.trim());
            if (!uri.isAbsolute() || uri.isOpaque()) {
                return Optional.empty();
            }
            String scheme = uri.getScheme();
            if (scheme == null) {
                return Optional.empty();
            }
            scheme = scheme.toLowerCase(Locale.ROOT);
            if (!scheme.equals("http") && !scheme.equals("https")) {
                return Optional.empty();
            }
            if (StringUtils.hasText(uri.getRawQuery())
                    || StringUtils.hasText(uri.getRawFragment())
                    || StringUtils.hasText(uri.getRawUserInfo())
                    || !StringUtils.hasText(uri.getHost())) {
                return Optional.empty();
            }

            String path = uri.getPath();
            if (!StringUtils.hasText(path) || "/".equals(path)) {
                path = CALLBACK_PATH;
            }
            if (!CALLBACK_PATH.equals(path)) {
                return Optional.empty();
            }

            String host = uri.getHost().toLowerCase(Locale.ROOT);
            int port = uri.getPort();
            StringBuilder normalized = new StringBuilder()
                    .append(scheme)
                    .append("://")
                    .append(host);
            if (port != -1 && !isDefaultPort(scheme, port)) {
                normalized.append(':').append(port);
            }
            normalized.append(CALLBACK_PATH);
            return Optional.of(normalized.toString());
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private boolean isDefaultPort(String scheme, int port) {
        return ("http".equals(scheme) && port == 80)
                || ("https".equals(scheme) && port == 443);
    }
}
