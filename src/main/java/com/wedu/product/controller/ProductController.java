package com.wedu.product.controller;

import com.wedu.global.response.ApiResponse;
import com.wedu.product.domain.ProductCategory;
import com.wedu.product.dto.ProductDetailResponse;
import com.wedu.product.dto.ProductSummaryResponse;
import com.wedu.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 프로포즈 편집샵 상품 목록/검색/필터 HTTP 요청을 처리한다. */
@Tag(name = "Product", description = "상품 목록/검색/필터")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /** 카테고리/키워드/가격 범위로 상품 목록을 검색하고 정렬·페이징해서 반환한다. */
    @Operation(summary = "상품 목록 조회 (검색/필터/정렬/페이징)")
    @GetMapping
    public ApiResponse<List<ProductSummaryResponse>> search(
            @Parameter(description = "상품 카테고리") @RequestParam(required = false) ProductCategory category,
            @Parameter(description = "상품명 검색어") @RequestParam(required = false) String keyword,
            @Parameter(description = "최소 가격") @RequestParam(required = false) Integer minPrice,
            @Parameter(description = "최대 가격") @RequestParam(required = false) Integer maxPrice,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(productService.search(category, keyword, minPrice, maxPrice, pageable));
    }

    /** 상품 상세 정보를 조회한다. */
    @Operation(summary = "상품 상세 조회")
    @GetMapping("/{productId}")
    public ApiResponse<ProductDetailResponse> getDetail(@PathVariable Long productId) {
        return ApiResponse.ok(productService.getDetail(productId));
    }
}
