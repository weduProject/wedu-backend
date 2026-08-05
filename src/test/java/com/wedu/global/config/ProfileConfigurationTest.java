package com.wedu.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

class ProfileConfigurationTest {

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
