package com.wedu.planner.controller;

import com.wedu.friend.service.FriendAccessService;
import com.wedu.friend.service.ShareLinkService;
import com.wedu.global.response.ApiResponse;
import com.wedu.planner.dto.DDayRequest;
import com.wedu.planner.dto.DDayResponse;
import com.wedu.planner.service.DDayService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final FriendAccessService friendAccessService;
    private final ShareLinkService shareLinkService;

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

    /** 친구의 결혼식 D-day를 조회한다. */
    @Hidden
    @Operation(summary = "친구의 D-day 조회 (친구만 가능)")
    @GetMapping("/friends/{ownerUserId}")
    public ApiResponse<DDayResponse> getFriendDDay(
            @AuthenticationPrincipal Long userId, @PathVariable Long ownerUserId) {
        friendAccessService.assertEditable(userId, ownerUserId);
        return ApiResponse.ok(dDayService.getMyDDay(ownerUserId));
    }

    /** 친구의 결혼식 날짜를 함께 수정한다. */
    @Hidden
    @Operation(summary = "친구의 D-day 수정 (친구만 가능)")
    @PatchMapping("/friends/{ownerUserId}")
    public ApiResponse<DDayResponse> updateFriendDDay(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long ownerUserId,
            @Valid @RequestBody DDayRequest request) {
        friendAccessService.assertEditable(userId, ownerUserId);
        return ApiResponse.ok(dDayService.update(ownerUserId, request.weddingDate()));
    }

    /** 공유 링크로 D-day를 조회 전용으로 확인한다(로그인 불필요). */
    @Hidden
    @Operation(summary = "공유 링크로 D-day 조회 (조회 전용)")
    @GetMapping("/shared/{token}")
    public ApiResponse<DDayResponse> getSharedDDay(@PathVariable String token) {
        return ApiResponse.ok(dDayService.getMyDDay(shareLinkService.resolveOwnerId(token)));
    }
}
