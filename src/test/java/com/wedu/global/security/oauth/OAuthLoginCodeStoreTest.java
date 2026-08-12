package com.wedu.global.security.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wedu.auth.dto.SocialLoginResult;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class OAuthLoginCodeStoreTest {

    private static final long TTL_SECONDS = 120;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private OAuthLoginCodeStore store;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        store = new OAuthLoginCodeStore(redisTemplate, objectMapper, TTL_SECONDS);
    }

    @Test
    @DisplayName("TTL 이 0 이하면 생성에 실패한다")
    void rejectNonPositiveTtl() {
        assertThatThrownBy(() -> new OAuthLoginCodeStore(redisTemplate, objectMapper, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be positive");
        assertThatThrownBy(() -> new OAuthLoginCodeStore(redisTemplate, objectMapper, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be positive");
    }

    @Test
    @DisplayName("발급한 코드는 TTL 과 함께 Redis 에 저장되고 한 번만 교환할 수 있다")
    void issueAndConsumeOnce() throws Exception {
        SocialLoginResult result =
                SocialLoginResult.of("token", 1L, "a@example.com", "닉", false);

        String code = store.issue(result);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations)
                .set(keyCaptor.capture(), valueCaptor.capture(), eq(Duration.ofSeconds(TTL_SECONDS)));
        assertThat(keyCaptor.getValue()).isEqualTo(OAuthLoginCodeStore.KEY_PREFIX + code);
        assertThat(objectMapper.readValue(valueCaptor.getValue(), SocialLoginResult.class))
                .isEqualTo(result);

        when(valueOperations.getAndDelete(OAuthLoginCodeStore.KEY_PREFIX + code))
                .thenReturn(valueCaptor.getValue())
                .thenReturn(null);

        assertThat(store.consume(code).accessToken()).isEqualTo("token");
        assertThatThrownBy(() -> store.consume(code))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_OAUTH_CODE_INVALID);
    }

    @Test
    @DisplayName("존재하지 않거나 만료된 코드는 거절한다")
    void rejectUnknownOrExpiredCode() {
        when(valueOperations.getAndDelete(OAuthLoginCodeStore.KEY_PREFIX + "missing"))
                .thenReturn(null);

        assertThatThrownBy(() -> store.consume("missing"))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_OAUTH_CODE_INVALID);
    }

    @Test
    @DisplayName("손상된 페이로드는 유효하지 않은 코드로 거절한다")
    void rejectCorruptPayload() {
        when(valueOperations.getAndDelete(OAuthLoginCodeStore.KEY_PREFIX + "bad"))
                .thenReturn("{not-json");

        assertThatThrownBy(() -> store.consume("bad"))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_OAUTH_CODE_INVALID);
    }
}
