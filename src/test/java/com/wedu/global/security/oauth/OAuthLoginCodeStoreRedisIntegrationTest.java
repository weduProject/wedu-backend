package com.wedu.global.security.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wedu.auth.dto.SocialLoginResult;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class OAuthLoginCodeStoreRedisIntegrationTest {

    @Container
    @ServiceConnection
    static GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

    @Autowired
    private OAuthLoginCodeStore store;

    @Test
    @DisplayName("Redis 에서 코드를 발급·1회 소비하고 재사용을 거절한다")
    void issueConsumeOnceAgainstRedis() {
        SocialLoginResult result =
                SocialLoginResult.of("token", 1L, "a@example.com", "닉", false);

        String code = store.issue(result);
        assertThat(store.consume(code)).isEqualTo(result);
        assertThatThrownBy(() -> store.consume(code))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_OAUTH_CODE_INVALID);
    }
}
