package com.wedu.product.controller;

import com.wedu.global.response.ApiResponse;
import com.wedu.product.dto.PopularProductRankingResponse;
import com.wedu.product.service.PopularProductRankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인기 상품 순위를 즉시 갱신하는 개발용 엔드포인트.
 *
 * <p>운영에서는 스케줄러만 순위를 만든다. 확인용 수동 갱신이 필요한 local/dev 에서만 열고,
 * 공개 경로(`/api/products/**`)를 피해 로그인한 사용자만 호출할 수 있게 둔다.
 */
@Tag(name = "PopularProduct", description = "인기 추천 상품")
@Profile("(local | dev) & !prod & !staging")
@RestController
@RequestMapping("/api/internal/popular-products")
@RequiredArgsConstructor
public class PopularProductRankingController {

    private final PopularProductRankingService popularProductRankingService;

    @Operation(summary = "인기 상품 순위 즉시 갱신 (local/dev 전용)")
    @PostMapping("/refresh")
    public ApiResponse<PopularProductRankingResponse> refresh() {
        return ApiResponse.ok(PopularProductRankingResponse.of(popularProductRankingService.refresh()));
    }
}
