package com.wedu.planner.controller;

import com.wedu.friend.service.FriendAccessService;
import com.wedu.friend.service.ShareLinkService;
import com.wedu.global.response.ApiResponse;
import com.wedu.planner.domain.CalendarEventCategory;
import com.wedu.planner.dto.CalendarEventCreateRequest;
import com.wedu.planner.dto.CalendarEventResponse;
import com.wedu.planner.dto.CalendarEventUpdateRequest;
import com.wedu.planner.service.CalendarEventService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 인증 사용자의 캘린더 일정 HTTP 요청을 처리한다. */
@Tag(name = "Calendar", description = "기념일 및 준비 일정 관리")
@RestController
@RequestMapping("/api/calendar-events")
@RequiredArgsConstructor
public class CalendarEventController {

    private final CalendarEventService calendarEventService;
    private final FriendAccessService friendAccessService;
    private final ShareLinkService shareLinkService;

    /** 날짜 단위 캘린더 일정을 생성한다. */
    @Operation(summary = "캘린더 일정 생성")
    @PostMapping
    public ApiResponse<CalendarEventResponse> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CalendarEventCreateRequest request) {
        return ApiResponse.ok(
                calendarEventService.create(userId, request));
    }

    /** 지정한 연월의 내 일정을 조회한다. */
    @Operation(summary = "월별 캘린더 일정 조회")
    @GetMapping
    public ApiResponse<List<CalendarEventResponse>> getMonthlyEvents(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "조회 연도", example = "2026")
            @RequestParam Integer year,
            @Parameter(description = "조회 월(1~12)", example = "7")
            @RequestParam Integer month,
            @Parameter(description = "일정 카테고리")
            @RequestParam(required = false) CalendarEventCategory category) {
        return ApiResponse.ok(
                calendarEventService.getMonthlyEvents(userId, year, month, category));
    }

    /** 오늘 이후의 다가오는 내 일정을 조회한다. */
    @Hidden
    @Operation(summary = "다가오는 캘린더 일정 조회")
    @GetMapping("/upcoming")
    public ApiResponse<List<CalendarEventResponse>> getUpcomingEvents(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "일정 카테고리")
            @RequestParam(required = false) CalendarEventCategory category,
            @Parameter(description = "조회 개수(1~50)", example = "10")
            @RequestParam(defaultValue = "10") Integer limit) {
        return ApiResponse.ok(calendarEventService.getUpcomingEvents(userId, category, limit));
    }

    /** 소유한 캘린더 일정을 수정한다. */
    @Operation(summary = "캘린더 일정 수정")
    @PutMapping("/{eventId}")
    public ApiResponse<CalendarEventResponse> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody CalendarEventUpdateRequest request) {
        return ApiResponse.ok(calendarEventService.update(userId, eventId, request));
    }

    /** 소유한 캘린더 일정을 삭제한다. */
    @Operation(summary = "캘린더 일정 삭제")
    @DeleteMapping("/{eventId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long eventId) {
        calendarEventService.delete(userId, eventId);
        return ApiResponse.ok();
    }

    /** 친구의 캘린더에 일정을 함께 추가한다. */
    @Hidden
    @Operation(summary = "친구 캘린더 일정 생성 (친구만 가능)")
    @PostMapping("/friends/{ownerUserId}")
    public ApiResponse<CalendarEventResponse> createFriendEvent(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long ownerUserId,
            @Valid @RequestBody CalendarEventCreateRequest request) {
        friendAccessService.assertEditable(userId, ownerUserId);
        return ApiResponse.ok(calendarEventService.create(ownerUserId, request));
    }

    /** 친구의 월별 캘린더 일정을 조회한다. */
    @Hidden
    @Operation(summary = "친구 캘린더 일정 조회 (친구만 가능)")
    @GetMapping("/friends/{ownerUserId}")
    public ApiResponse<List<CalendarEventResponse>> getFriendMonthlyEvents(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long ownerUserId,
            @RequestParam Integer year,
            @RequestParam Integer month,
            @RequestParam(required = false) CalendarEventCategory category) {
        friendAccessService.assertEditable(userId, ownerUserId);
        return ApiResponse.ok(
                calendarEventService.getMonthlyEvents(ownerUserId, year, month, category));
    }

    /** 친구의 캘린더 일정을 함께 수정한다. */
    @Hidden
    @Operation(summary = "친구 캘린더 일정 수정 (친구만 가능)")
    @PutMapping("/friends/{ownerUserId}/{eventId}")
    public ApiResponse<CalendarEventResponse> updateFriendEvent(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long ownerUserId,
            @PathVariable Long eventId,
            @Valid @RequestBody CalendarEventUpdateRequest request) {
        friendAccessService.assertEditable(userId, ownerUserId);
        return ApiResponse.ok(calendarEventService.update(ownerUserId, eventId, request));
    }

    /** 친구의 캘린더 일정을 함께 삭제한다. */
    @Hidden
    @Operation(summary = "친구 캘린더 일정 삭제 (친구만 가능)")
    @DeleteMapping("/friends/{ownerUserId}/{eventId}")
    public ApiResponse<Void> deleteFriendEvent(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long ownerUserId,
            @PathVariable Long eventId) {
        friendAccessService.assertEditable(userId, ownerUserId);
        calendarEventService.delete(ownerUserId, eventId);
        return ApiResponse.ok();
    }

    /** 공유 링크로 월별 캘린더 일정을 조회 전용으로 확인한다(로그인 불필요). */
    @Hidden
    @Operation(summary = "공유 링크로 캘린더 일정 조회 (조회 전용)")
    @GetMapping("/shared/{token}")
    public ApiResponse<List<CalendarEventResponse>> getSharedMonthlyEvents(
            @PathVariable String token,
            @RequestParam Integer year,
            @RequestParam Integer month,
            @RequestParam(required = false) CalendarEventCategory category) {
        Long ownerUserId = shareLinkService.resolveOwnerId(token);
        return ApiResponse.ok(
                calendarEventService.getMonthlyEvents(ownerUserId, year, month, category));
    }
}
