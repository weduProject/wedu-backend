package com.wedu.recommendation.controller;

import com.wedu.global.response.ApiResponse;
import com.wedu.recommendation.service.PsychologicalTestService;
import com.wedu.recommendation.dto.PsychologicalTestSubmitRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 인증 사용자의 심리테스트 응답 제출 요청을 처리한다. */
@Tag(name = "Psychological Test", description = "심리테스트 응답 관리")
@RestController
@RequestMapping("/api/psychological-tests")
@RequiredArgsConstructor
public class PsychologicalTestController {

    private final PsychologicalTestService psychologicalTestService;

    /** 로그인 사용자의 심리테스트 응답을 제출한다. */
    @Operation(summary = "심리테스트 응답 제출")
    @PostMapping
    public ApiResponse<Long> submit(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PsychologicalTestSubmitRequest request) {

        return ApiResponse.ok(
                psychologicalTestService.submit(userId, request)
        );
    }
}