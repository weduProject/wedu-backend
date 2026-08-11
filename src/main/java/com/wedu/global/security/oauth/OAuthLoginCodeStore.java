package com.wedu.global.security.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wedu.auth.dto.SocialLoginResult;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 소셜 로그인 직후 JWT 를 URL 에 노출하지 않기 위한 일회용 교환 코드를 Redis 에 보관한다.
 *
 * <p>TTL 만료와 {@code GETDEL} 기반 원자적 소비로 다중 인스턴스·재시작에서도 일회성을 보장한다.
 * Redis 6.2+ 가 필요하다.
 */
@Component
public class OAuthLoginCodeStore {

    static final String KEY_PREFIX = "wedu:oauth:login-code:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration ttl;

    public OAuthLoginCodeStore(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            @Value("${wedu.oauth2.login-code-ttl-seconds:120}") long ttlSeconds) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.ttl = Duration.ofSeconds(ttlSeconds);
    }

    public String issue(SocialLoginResult result) {
        String code = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(key(code), serialize(result), ttl);
        return code;
    }

    public SocialLoginResult consume(String code) {
        String payload = redisTemplate.opsForValue().getAndDelete(key(code));
        if (payload == null) {
            throw new BusinessException(ErrorCode.AUTH_OAUTH_CODE_INVALID);
        }
        return deserialize(payload);
    }

    private static String key(String code) {
        return KEY_PREFIX + code;
    }

    private String serialize(SocialLoginResult result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize OAuth login result", e);
        }
    }

    private SocialLoginResult deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, SocialLoginResult.class);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.AUTH_OAUTH_CODE_INVALID);
        }
    }
}
