package com.wedu.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wedu.auth.dto.SocialLoginResult;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OAuthLoginCodeStoreTest {

    private final OAuthLoginCodeStore store = new OAuthLoginCodeStore();

    @Test
    @DisplayName("발급한 코드는 한 번만 교환할 수 있다")
    void issueAndConsumeOnce() {
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
        assertThatThrownBy(() -> store.consume("missing"))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_OAUTH_CODE_INVALID);
    }
}
