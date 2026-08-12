package com.wedu.recommendation.controller;

import com.wedu.global.response.ApiResponse;
import com.wedu.recommendation.service.RecommendationService;
import com.wedu.recommendation.dto.AiRecommendationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 인증 사용자의 심리테스트 기반 맞춤 추천 요청을 처리한다. */
@Tag(name = "Recommendation", description = "심리테스트 기반 맞춤 상품 추천")
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    /** 로그인 사용자의 심리테스트 결과를 기반으로 맞춤 상품을 추천한다. */
    @Operation(summary = "심리테스트 기반 맞춤 상품 추천")
    @GetMapping
    public ApiResponse<AiRecommendationResponse> recommend(
            @AuthenticationPrincipal Long userId) {

        return ApiResponse.ok(
                recommendationService.recommend(userId)
        );
    }
}