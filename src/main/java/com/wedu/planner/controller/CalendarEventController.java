package com.wedu.planner.controller;

import com.wedu.global.response.ApiResponse;
import com.wedu.planner.dto.CalendarEventCreateRequest;
import com.wedu.planner.dto.CalendarEventResponse;
import com.wedu.planner.service.CalendarEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
                calendarEventService.create(userId, request.title(), request.eventDate()));
    }

    /** 지정한 연월의 내 일정을 조회한다. */
    @Operation(summary = "월별 캘린더 일정 조회")
    @GetMapping
    public ApiResponse<List<CalendarEventResponse>> getMonthlyEvents(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "조회 연도", example = "2026")
            @RequestParam Integer year,
            @Parameter(description = "조회 월(1~12)", example = "7")
            @RequestParam Integer month) {
        return ApiResponse.ok(calendarEventService.getMonthlyEvents(userId, year, month));
    }
}
