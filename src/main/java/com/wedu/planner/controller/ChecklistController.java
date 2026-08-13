package com.wedu.planner.controller;

import com.wedu.friend.service.FriendAccessService;
import com.wedu.friend.service.ShareLinkService;
import com.wedu.global.response.ApiResponse;
import com.wedu.planner.domain.ChecklistCategory;
import com.wedu.planner.dto.ChecklistCompletionRequest;
import com.wedu.planner.dto.ChecklistItemCreateRequest;
import com.wedu.planner.dto.ChecklistItemResponse;
import com.wedu.planner.dto.ChecklistItemUpdateRequest;
import com.wedu.planner.dto.ChecklistOverviewResponse;
import com.wedu.planner.service.ChecklistService;
import io.swagger.v3.oas.annotations.Hidden;
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
    private final FriendAccessService friendAccessService;
    private final ShareLinkService shareLinkService;

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

    /** 친구의 체크리스트에 항목을 함께 추가한다. */
    @Hidden
    @Operation(summary = "친구 체크리스트 항목 생성 (친구만 가능)")
    @PostMapping("/friends/{ownerUserId}")
    public ApiResponse<ChecklistItemResponse> createFriendItem(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long ownerUserId,
            @Valid @RequestBody ChecklistItemCreateRequest request) {
        friendAccessService.assertEditable(userId, ownerUserId);
        return ApiResponse.ok(checklistService.create(ownerUserId, request));
    }

    /** 친구의 체크리스트와 진행률을 조회한다. */
    @Hidden
    @Operation(summary = "친구 체크리스트 조회 (친구만 가능)")
    @GetMapping("/friends/{ownerUserId}")
    public ApiResponse<ChecklistOverviewResponse> getFriendChecklist(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long ownerUserId,
            @RequestParam(required = false) ChecklistCategory category) {
        friendAccessService.assertEditable(userId, ownerUserId);
        return ApiResponse.ok(checklistService.getChecklist(ownerUserId, category));
    }

    /** 친구의 체크리스트 항목을 함께 수정한다. */
    @Hidden
    @Operation(summary = "친구 체크리스트 항목 수정 (친구만 가능)")
    @PutMapping("/friends/{ownerUserId}/{itemId}")
    public ApiResponse<ChecklistItemResponse> updateFriendItem(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long ownerUserId,
            @PathVariable Long itemId,
            @Valid @RequestBody ChecklistItemUpdateRequest request) {
        friendAccessService.assertEditable(userId, ownerUserId);
        return ApiResponse.ok(checklistService.update(ownerUserId, itemId, request));
    }

    /** 친구의 체크리스트 완료 상태를 함께 변경한다. */
    @Hidden
    @Operation(summary = "친구 체크리스트 완료 상태 변경 (친구만 가능)")
    @PatchMapping("/friends/{ownerUserId}/{itemId}/completion")
    public ApiResponse<ChecklistItemResponse> changeFriendCompletion(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long ownerUserId,
            @PathVariable Long itemId,
            @Valid @RequestBody ChecklistCompletionRequest request) {
        friendAccessService.assertEditable(userId, ownerUserId);
        return ApiResponse.ok(checklistService.changeCompletion(ownerUserId, itemId, request));
    }

    /** 친구의 체크리스트 항목을 함께 삭제한다. */
    @Hidden
    @Operation(summary = "친구 체크리스트 항목 삭제 (친구만 가능)")
    @DeleteMapping("/friends/{ownerUserId}/{itemId}")
    public ApiResponse<Void> deleteFriendItem(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long ownerUserId,
            @PathVariable Long itemId) {
        friendAccessService.assertEditable(userId, ownerUserId);
        checklistService.delete(ownerUserId, itemId);
        return ApiResponse.ok();
    }

    /** 공유 링크로 체크리스트를 조회 전용으로 확인한다(로그인 불필요). */
    @Hidden
    @Operation(summary = "공유 링크로 체크리스트 조회 (조회 전용)")
    @GetMapping("/shared/{token}")
    public ApiResponse<ChecklistOverviewResponse> getSharedChecklist(
            @PathVariable String token,
            @RequestParam(required = false) ChecklistCategory category) {
        Long ownerUserId = shareLinkService.resolveOwnerId(token);
        return ApiResponse.ok(checklistService.getChecklist(ownerUserId, category));
    }
}
