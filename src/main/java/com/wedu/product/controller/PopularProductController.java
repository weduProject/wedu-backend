package com.wedu.product.controller;

import com.wedu.global.response.ApiResponse;
import com.wedu.product.dto.PopularProductResponse;
import com.wedu.product.service.PopularProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 인기 상품(크롤링 수집 결과) 조회 HTTP 요청을 처리한다. */
@Tag(name = "PopularProduct", description = "인기 추천 상품")
@RestController
@RequestMapping("/api/products/popular")
@RequiredArgsConstructor
public class PopularProductController {

    private final PopularProductService popularProductService;

    /** 순위 기준 인기 상품 목록을 조회한다. */
    @Operation(summary = "인기 상품 목록 조회")
    @GetMapping
    public ApiResponse<List<PopularProductResponse>> getPopularProducts() {
        return ApiResponse.ok(popularProductService.getPopularProducts());
    }
}
