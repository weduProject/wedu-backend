package com.wedu.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.wedu.auth.controller.AuthController;
import com.wedu.auth.service.TempLoginService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

abstract class TempLoginProfileTest {

    @Autowired
    private ApplicationContext applicationContext;

    void assertTempLoginBeansRegistered() {
        assertThat(applicationContext.getBeansOfType(AuthController.class)).hasSize(1);
        assertThat(applicationContext.getBeansOfType(TempLoginService.class)).hasSize(1);
    }

    void assertTempLoginBeansNotRegistered() {
        assertThat(applicationContext.getBeansOfType(AuthController.class)).isEmpty();
        assertThat(applicationContext.getBeansOfType(TempLoginService.class)).isEmpty();
    }
}

@SpringBootTest
@ActiveProfiles("local")
class TempLoginLocalProfileTest extends TempLoginProfileTest {

    @Test
    void registerTempLoginBeansOnLocalProfile() {
        assertTempLoginBeansRegistered();
    }
}

@SpringBootTest
@ActiveProfiles("dev")
class TempLoginDevProfileTest extends TempLoginProfileTest {

    @Test
    void registerTempLoginBeansOnDevProfile() {
        assertTempLoginBeansRegistered();
    }
}

@SpringBootTest(
        properties = {
            "CORS_ALLOWED_ORIGINS=https://frontend.example.com",
            "REDIS_HOST=localhost"
        })
@ActiveProfiles("prod")
class TempLoginProdProfileTest extends TempLoginProfileTest {

    @Test
    void doNotRegisterTempLoginBeansOnProdProfile() {
        assertTempLoginBeansNotRegistered();
    }
}

@SpringBootTest
@ActiveProfiles("staging")
class TempLoginStagingProfileTest extends TempLoginProfileTest {

    @Test
    void doNotRegisterTempLoginBeansOnStagingProfile() {
        assertTempLoginBeansNotRegistered();
    }
}

@SpringBootTest(
        properties = {
            "CORS_ALLOWED_ORIGINS=https://frontend.example.com",
            "REDIS_HOST=localhost"
        })
@ActiveProfiles({"prod", "dev"})
class TempLoginProdDevProfileTest extends TempLoginProfileTest {

    @Test
    void doNotRegisterTempLoginBeansWhenProdAndDevProfilesAreActiveTogether() {
        assertTempLoginBeansNotRegistered();
    }
}

@SpringBootTest
@ActiveProfiles({"staging", "local"})
class TempLoginStagingLocalProfileTest extends TempLoginProfileTest {

    @Test
    void doNotRegisterTempLoginBeansWhenStagingAndLocalProfilesAreActiveTogether() {
        assertTempLoginBeansNotRegistered();
    }
}

@SpringBootTest
class TempLoginNoProfileTest extends TempLoginProfileTest {

    @Test
    void doNotRegisterTempLoginBeansWithoutProfile() {
        assertTempLoginBeansNotRegistered();
    }
}
