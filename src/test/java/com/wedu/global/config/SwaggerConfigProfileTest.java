package com.wedu.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class SwaggerConfigProfileTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(SwaggerConfig.class);

    @Test
    void registerOpenApiBeanOnLocalProfile() {
        contextRunner
                .withInitializer(context -> context.getEnvironment().setActiveProfiles("local"))
                .run(context -> assertThat(context).hasSingleBean(OpenAPI.class));
    }

    @Test
    void registerOpenApiBeanOnDevProfile() {
        contextRunner
                .withInitializer(context -> context.getEnvironment().setActiveProfiles("dev"))
                .run(context -> assertThat(context).hasSingleBean(OpenAPI.class));
    }

    @Test
    void doNotRegisterOpenApiBeanOnProdProfile() {
        contextRunner
                .withInitializer(context -> context.getEnvironment().setActiveProfiles("prod"))
                .run(context -> assertThat(context).doesNotHaveBean(OpenAPI.class));
    }

    @Test
    void doNotRegisterOpenApiBeanWithoutProfile() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(OpenAPI.class));
    }
}
