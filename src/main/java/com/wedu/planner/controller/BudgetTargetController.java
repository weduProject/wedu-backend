package com.wedu.planner.controller;

import com.wedu.friend.service.FriendAccessService;
import com.wedu.global.response.ApiResponse;
import com.wedu.planner.dto.BudgetTargetRequest;
import com.wedu.planner.dto.BudgetTargetResponse;
import com.wedu.planner.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 사용자별 웨딩 전체 목표 예산 설정 요청을 처리한다. */
@Tag(name = "Budget", description = "웨딩 준비 예산 및 지출 관리")
@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetTargetController {

    private final BudgetService budgetService;
    private final FriendAccessService friendAccessService;

    @Operation(summary = "내 전체 목표 예산 설정 또는 변경")
    @PutMapping("/me")
    public ApiResponse<BudgetTargetResponse> setMyTarget(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody BudgetTargetRequest request) {
        return ApiResponse.ok(budgetService.setTarget(userId, request));
    }

    @Operation(summary = "친구 전체 목표 예산 설정 또는 변경 (친구만 가능)")
    @PutMapping("/friends/{ownerUserId}")
    public ApiResponse<BudgetTargetResponse> setFriendTarget(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long ownerUserId,
            @Valid @RequestBody BudgetTargetRequest request) {
        friendAccessService.assertEditable(userId, ownerUserId);
        return ApiResponse.ok(budgetService.setTarget(ownerUserId, request));
    }
}
