package com.wedu.planner.controller;

import com.wedu.global.response.ApiResponse;
import com.wedu.planner.dto.BudgetCompletionRequest;
import com.wedu.planner.dto.BudgetItemCreateRequest;
import com.wedu.planner.dto.BudgetItemResponse;
import com.wedu.planner.dto.BudgetItemUpdateRequest;
import com.wedu.planner.dto.BudgetOverviewResponse;
import com.wedu.planner.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 인증 사용자의 웨딩 예산과 지출 항목 HTTP 요청을 처리한다. */
@Tag(name = "Budget", description = "웨딩 준비 예산 및 지출 관리")
@RestController
@RequestMapping("/api/budget-items")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @Operation(summary = "예산 항목 생성")
    @PostMapping
    public ApiResponse<BudgetItemResponse> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody BudgetItemCreateRequest request) {
        return ApiResponse.ok(budgetService.create(userId, request));
    }

    @Operation(summary = "전체 및 카테고리별 예산 현황 조회")
    @GetMapping
    public ApiResponse<BudgetOverviewResponse> getOverview(
            @AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(budgetService.getOverview(userId));
    }

    @Operation(summary = "예산 항목 정보 수정")
    @PutMapping("/{itemId}")
    public ApiResponse<BudgetItemResponse> update(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "예산 항목 ID")
            @PathVariable Long itemId,
            @Valid @RequestBody BudgetItemUpdateRequest request) {
        return ApiResponse.ok(budgetService.update(userId, itemId, request));
    }

    @Operation(summary = "예산 항목 결제 완료 상태 변경")
    @PatchMapping("/{itemId}/completion")
    public ApiResponse<BudgetItemResponse> changeCompletion(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "예산 항목 ID")
            @PathVariable Long itemId,
            @Valid @RequestBody BudgetCompletionRequest request) {
        return ApiResponse.ok(budgetService.changeCompletion(userId, itemId, request));
    }

    @Operation(summary = "예산 항목 삭제")
    @DeleteMapping("/{itemId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "예산 항목 ID")
            @PathVariable Long itemId) {
        budgetService.delete(userId, itemId);
        return ApiResponse.ok();
    }
}
