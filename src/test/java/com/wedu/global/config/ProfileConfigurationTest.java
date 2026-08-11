package com.wedu.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        properties = {
            "CORS_ALLOWED_ORIGINS=https://frontend.example.com",
            "REDIS_HOST=localhost",
            "PUBLIC_BASE_URL=https://api.example.com"
        })
@ActiveProfiles("prod")
class ProfileConfigurationTest {

    @Autowired
    private Environment environment;

    @Test
    void prodProfileDisablesSwaggerAndUsesNonDebugLogging() {
        assertThat(environment.getProperty("springdoc.api-docs.enabled")).isEqualTo("false");
        assertThat(environment.getProperty("springdoc.swagger-ui.enabled")).isEqualTo("false");
        assertThat(environment.getProperty("logging.level.com.wedu")).isEqualTo("INFO");
        assertThat(environment.getProperty("logging.level.org.hibernate.SQL")).isEqualTo("WARN");
        assertThat(environment.getProperty("wedu.cors.allowed-origins"))
                .isEqualTo("https://frontend.example.com");
    }
}
