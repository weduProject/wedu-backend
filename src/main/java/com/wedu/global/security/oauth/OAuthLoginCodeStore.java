package com.wedu.global.security.oauth;

import com.wedu.auth.dto.SocialLoginResult;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * 소셜 로그인 직후 JWT 를 URL 에 노출하지 않기 위한 일회용 교환 코드를 잠깐 보관한다.
 */
@Component
public class OAuthLoginCodeStore {

    private static final long TTL_SECONDS = 120;

    private final Clock clock;
    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    public OAuthLoginCodeStore(Clock clock) {
        this.clock = clock;
    }

    public String issue(SocialLoginResult result) {
        purgeExpired();
        String code = UUID.randomUUID().toString().replace("-", "");
        store.put(code, new Entry(result, clock.instant().plusSeconds(TTL_SECONDS)));
        return code;
    }

    public SocialLoginResult consume(String code) {
        purgeExpired();
        Entry entry = store.remove(code);
        Instant now = clock.instant();
        if (entry == null || !entry.expiresAt().isAfter(now)) {
            throw new BusinessException(ErrorCode.AUTH_OAUTH_CODE_INVALID);
        }
        return entry.result();
    }

    private void purgeExpired() {
        Instant now = clock.instant();
        store.entrySet().removeIf(e -> !e.getValue().expiresAt().isAfter(now));
    }

    private record Entry(SocialLoginResult result, Instant expiresAt) {
    }
}
