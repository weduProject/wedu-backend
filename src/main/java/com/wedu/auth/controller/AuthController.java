package com.wedu.auth.controller;

import com.wedu.auth.dto.TempLoginRequest;
import com.wedu.auth.dto.TempLoginResponse;
import com.wedu.auth.service.TempLoginService;
import com.wedu.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증")
@RestController
@RequestMapping("/api/auth")
@Profile("!prod")
@RequiredArgsConstructor
public class AuthController {

    private final TempLoginService tempLoginService;

    @Operation(summary = "임시 로그인")
    @PostMapping("/temp-login")
    public ApiResponse<TempLoginResponse> tempLogin(
            @Valid @RequestBody TempLoginRequest request) {
        return ApiResponse.ok(tempLoginService.login(request));
    }
}
