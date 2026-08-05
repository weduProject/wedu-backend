package com.wedu.friend.controller;

import com.wedu.friend.dto.ShareLinkResponse;
import com.wedu.friend.service.ShareLinkService;
import com.wedu.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 내 플래너 조회 전용 공유 링크 발급/조회 HTTP 요청을 처리한다. */
@Tag(name = "ShareLink", description = "플래너 조회 전용 공유 링크")
@RestController
@RequestMapping("/api/share-links")
@RequiredArgsConstructor
public class ShareLinkController {

    private final ShareLinkService shareLinkService;

    /** 내 공유 링크를 조회한다(없으면 새로 발급). */
    @Operation(summary = "내 공유 링크 조회/발급")
    @GetMapping("/me")
    public ApiResponse<ShareLinkResponse> getMyLink(@AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(shareLinkService.getOrIssueMyLink(userId));
    }

    /** 기존 링크를 무효화하고 새 토큰을 발급한다. */
    @Operation(summary = "내 공유 링크 재발급")
    @PostMapping("/me/reissue")
    public ApiResponse<ShareLinkResponse> reissueMyLink(@AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(shareLinkService.reissueMyLink(userId));
    }
}
