package com.wedu.auth.controller;

import com.wedu.auth.dto.OAuthTokenRequest;
import com.wedu.auth.dto.SocialLoginResult;
import com.wedu.auth.service.OAuthTokenExchangeService;
import com.wedu.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class OAuthTokenController {

    private final OAuthTokenExchangeService oAuthTokenExchangeService;

    @Operation(summary = "소셜 로그인 일회용 코드 → JWT 교환")
    @PostMapping("/oauth/token")
    public ApiResponse<SocialLoginResult> exchange(@Valid @RequestBody OAuthTokenRequest request) {
        return ApiResponse.ok(oAuthTokenExchangeService.exchange(request.code()));
    }
}
