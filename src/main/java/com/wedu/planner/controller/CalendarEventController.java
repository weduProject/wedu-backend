package com.wedu.planner.controller;

import com.wedu.global.response.ApiResponse;
import com.wedu.planner.domain.CalendarEventCategory;
import com.wedu.planner.dto.CalendarEventCreateRequest;
import com.wedu.planner.dto.CalendarEventResponse;
import com.wedu.planner.dto.CalendarEventUpdateRequest;
import com.wedu.planner.service.CalendarEventService;
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
}
