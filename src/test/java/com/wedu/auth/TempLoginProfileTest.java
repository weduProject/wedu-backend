package com.wedu.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.wedu.auth.controller.AuthController;
import com.wedu.auth.service.TempLoginService;
import com.wedu.global.security.jwt.JwtTokenProvider;
import com.wedu.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

class TempLoginProfileTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TempLoginProfileTestConfig.class);

    @ParameterizedTest
    @ValueSource(strings = {"local", "dev"})
    @DisplayName("local/dev 프로파일에서는 임시 로그인 빈을 등록한다")
    void registerTempLoginBeansOnAllowedProfiles(String profile) {
        contextRunner
                .withInitializer(context -> context.getEnvironment().setActiveProfiles(profile))
                .run(context -> assertThat(context)
                        .hasSingleBean(AuthController.class)
                        .hasSingleBean(TempLoginService.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"prod", "staging"})
    @DisplayName("local/dev 외 프로파일에서는 임시 로그인 빈을 등록하지 않는다")
    void doNotRegisterTempLoginBeansOnDisallowedProfiles(String profile) {
        contextRunner
                .withInitializer(context -> context.getEnvironment().setActiveProfiles(profile))
                .run(context -> assertThat(context)
                        .doesNotHaveBean(AuthController.class)
                        .doesNotHaveBean(TempLoginService.class));
    }

    @Test
    @DisplayName("프로파일이 없으면 임시 로그인 빈을 등록하지 않는다")
    void doNotRegisterTempLoginBeansWithoutProfile() {
        contextRunner.run(context -> assertThat(context)
                .doesNotHaveBean(AuthController.class)
                .doesNotHaveBean(TempLoginService.class));
    }

    @TestConfiguration
    @Import({AuthController.class, TempLoginService.class})
    static class TempLoginProfileTestConfig {

        @Bean
        UserRepository userRepository() {
            return mock(UserRepository.class);
        }

        @Bean
        JwtTokenProvider jwtTokenProvider() {
            return mock(JwtTokenProvider.class);
        }
    }
}
