package com.wedu.global.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wedu.auth.controller.EmailAuthController;
import com.wedu.auth.service.EmailAuthService;
import com.wedu.global.security.jwt.JwtTokenProvider;
import com.wedu.global.security.oauth.HttpCookieOAuth2AuthorizationRequestRepository;
import com.wedu.global.security.oauth.OAuth2FailureHandler;
import com.wedu.global.security.oauth.OAuth2SuccessHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EmailAuthController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "wedu.cors.allowed-origins=https://frontend.example.com")
class SecurityCorsConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailAuthService emailAuthService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockBean
    private OAuth2FailureHandler oAuth2FailureHandler;

    @MockBean
    private HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @Test
    void allowPreflightRequestFromConfiguredOrigin() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header(HttpHeaders.ORIGIN, "https://frontend.example.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization,Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://frontend.example.com"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PUT,PATCH,DELETE,OPTIONS"));
    }

    @Test
    void rejectPreflightRequestFromUnconfiguredOrigin() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header(HttpHeaders.ORIGIN, "https://evil.example.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden());
    }
}
