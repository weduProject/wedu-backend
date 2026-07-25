package com.wedu.global.security.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wedu.auth.dto.SocialLoginResult;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OAuthLoginCodeStoreTest {

    @Test
    @DisplayName("발급한 코드는 한 번만 교환할 수 있다")
    void issueAndConsumeOnce() {
        OAuthLoginCodeStore store = new OAuthLoginCodeStore(Clock.systemUTC());
        SocialLoginResult result =
                SocialLoginResult.of("token", 1L, "a@example.com", "닉", false);

        String code = store.issue(result);
        assertThat(store.consume(code).accessToken()).isEqualTo("token");
        assertThatThrownBy(() -> store.consume(code))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_OAUTH_CODE_INVALID);
    }

    @Test
    @DisplayName("존재하지 않는 코드는 거절한다")
    void rejectUnknownCode() {
        OAuthLoginCodeStore store = new OAuthLoginCodeStore(Clock.systemUTC());
        assertThatThrownBy(() -> store.consume("missing"))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_OAUTH_CODE_INVALID);
    }

    @Test
    @DisplayName("TTL 이 지나면 코드를 거절한다")
    void rejectExpiredCode() {
        Instant issuedAt = Instant.parse("2026-07-26T00:00:00Z");
        Clock fixedClock = Clock.fixed(issuedAt, ZoneOffset.UTC);
        OAuthLoginCodeStore store = new OAuthLoginCodeStore(fixedClock);

        SocialLoginResult result =
                SocialLoginResult.of("token", 1L, "a@example.com", "닉", false);
        String code = store.issue(result);

        Clock afterTtl = Clock.fixed(issuedAt.plusSeconds(120), ZoneOffset.UTC);
        org.springframework.test.util.ReflectionTestUtils.setField(store, "clock", afterTtl);

        assertThatThrownBy(() -> store.consume(code))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_OAUTH_CODE_INVALID);
    }
}
