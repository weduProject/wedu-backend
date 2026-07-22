package com.wedu.planner.controller;

import com.wedu.global.response.ApiResponse;
import com.wedu.planner.dto.DDayRequest;
import com.wedu.planner.dto.DDayResponse;
import com.wedu.planner.service.DDayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 인증 사용자의 결혼식 D-day HTTP 요청을 처리한다. */
@Tag(name = "D-day", description = "결혼식 D-day 관리")
@RestController
@RequestMapping("/api/ddays")
@RequiredArgsConstructor
public class DDayController {

    private final DDayService dDayService;

    /** 결혼식 날짜를 최초 등록한다. */
    @Operation(summary = "결혼식 D-day 생성")
    @PostMapping
    public ApiResponse<DDayResponse> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody DDayRequest request) {
        return ApiResponse.ok(dDayService.create(userId, request.weddingDate()));
    }

    /** 로그인 사용자의 결혼식 D-day를 조회한다. */
    @Operation(summary = "내 결혼식 D-day 조회")
    @GetMapping("/me")
    public ApiResponse<DDayResponse> getMyDDay(@AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(dDayService.getMyDDay(userId));
    }

    /** 로그인 사용자의 결혼식 날짜를 변경한다. */
    @Operation(summary = "내 결혼식 날짜 수정")
    @PatchMapping("/me")
    public ApiResponse<DDayResponse> update(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody DDayRequest request) {
        return ApiResponse.ok(dDayService.update(userId, request.weddingDate()));
    }

    /** 로그인 사용자의 결혼식 D-day를 삭제한다. */
    @Operation(summary = "내 결혼식 D-day 삭제")
    @DeleteMapping("/me")
    public ApiResponse<Void> delete(@AuthenticationPrincipal Long userId) {
        dDayService.delete(userId);
        return ApiResponse.ok();
    }
}
