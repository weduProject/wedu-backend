package com.wedu.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

class ProfileConfigurationTest {

    @ParameterizedTest
    @ValueSource(strings = {"application-local.yml", "application-dev.yml"})
    void developmentProfilesEnableSwaggerAndDebugLogging(String resourceName) {
        Properties properties = loadYaml(resourceName);

        assertThat(properties)
                .containsEntry("springdoc.api-docs.enabled", true)
                .containsEntry("springdoc.swagger-ui.enabled", true)
                .containsEntry("logging.level.com.wedu", "DEBUG")
                .containsEntry("logging.level.org.hibernate.SQL", "DEBUG");
    }

    @Test
    void prodProfileDisablesSwaggerAndUsesNonDebugLogging() {
        Properties properties = loadYaml("application-prod.yml");

        assertThat(properties)
                .containsEntry("springdoc.api-docs.enabled", false)
                .containsEntry("springdoc.swagger-ui.enabled", false)
                .containsEntry("logging.level.com.wedu", "INFO")
                .containsEntry("logging.level.org.hibernate.SQL", "WARN");
    }

    private Properties loadYaml(String resourceName) {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(new ClassPathResource(resourceName));
        return factory.getObject();
    }
}
