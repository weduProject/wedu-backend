package com.wedu.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger(OpenAPI 3) 문서 설정. {@code /swagger-ui/index.html} 에서 확인.
 *
 * <p>Bearer(JWT) 인증 스킴을 등록해, 문서 UI 에서 토큰을 넣고 인증 API 를 호출할 수 있게 한다.
 */
@Configuration
public class SwaggerConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI weduOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("WEDU API")
                        .description("심리테스트 기반 맞춤 프로포즈 추천 플랫폼 API")
                        .version("v0.0.1"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
