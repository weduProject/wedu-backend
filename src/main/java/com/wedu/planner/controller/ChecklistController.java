package com.wedu.planner.controller;

import com.wedu.global.response.ApiResponse;
import com.wedu.planner.domain.ChecklistCategory;
import com.wedu.planner.dto.ChecklistCompletionRequest;
import com.wedu.planner.dto.ChecklistItemCreateRequest;
import com.wedu.planner.dto.ChecklistItemResponse;
import com.wedu.planner.dto.ChecklistItemUpdateRequest;
import com.wedu.planner.dto.ChecklistOverviewResponse;
import com.wedu.planner.service.ChecklistService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 인증 사용자의 웨딩 준비 체크리스트 HTTP 요청을 처리한다. */
@Tag(name = "Checklist", description = "웨딩 준비 체크리스트 관리")
@RestController
@RequestMapping("/api/checklist-items")
@RequiredArgsConstructor
public class ChecklistController {

    private final ChecklistService checklistService;

    @Operation(summary = "체크리스트 항목 생성")
    @PostMapping
    public ApiResponse<ChecklistItemResponse> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ChecklistItemCreateRequest request) {
        return ApiResponse.ok(checklistService.create(userId, request));
    }

    @Operation(summary = "체크리스트와 전체 진행률 조회")
    @GetMapping
    public ApiResponse<ChecklistOverviewResponse> getChecklist(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "체크리스트 카테고리")
            @RequestParam(required = false) ChecklistCategory category) {
        return ApiResponse.ok(checklistService.getChecklist(userId, category));
    }

    @Operation(summary = "체크리스트 항목 정보 수정")
    @PutMapping("/{itemId}")
    public ApiResponse<ChecklistItemResponse> update(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "체크리스트 항목 ID")
            @PathVariable Long itemId,
            @Valid @RequestBody ChecklistItemUpdateRequest request) {
        return ApiResponse.ok(checklistService.update(userId, itemId, request));
    }

    @Operation(summary = "체크리스트 완료 상태 변경")
    @PatchMapping("/{itemId}/completion")
    public ApiResponse<ChecklistItemResponse> changeCompletion(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "체크리스트 항목 ID")
            @PathVariable Long itemId,
            @Valid @RequestBody ChecklistCompletionRequest request) {
        return ApiResponse.ok(checklistService.changeCompletion(userId, itemId, request));
    }

    @Operation(summary = "체크리스트 항목 삭제")
    @DeleteMapping("/{itemId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "체크리스트 항목 ID")
            @PathVariable Long itemId) {
        checklistService.delete(userId, itemId);
        return ApiResponse.ok();
    }
}
