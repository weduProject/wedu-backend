package com.wedu.invitation.controller;

import com.wedu.global.response.ApiResponse;
import com.wedu.invitation.dto.InvitationCreateRequest;
import com.wedu.invitation.dto.InvitationResponse;
import com.wedu.invitation.dto.InvitationUpdateRequest;
import com.wedu.invitation.service.InvitationService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 모바일 청첩장 생성·조회·수정·발행 HTTP 요청을 처리한다. */
@Hidden
@Tag(name = "Invitation", description = "모바일 청첩장")
@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    /** 새 청첩장을 생성한다. */
    @Operation(summary = "청첩장 생성")
    @PostMapping
    public ApiResponse<InvitationResponse> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody InvitationCreateRequest request) {

        return ApiResponse.ok(
                invitationService.create(userId, request)
        );
    }

    /** 로그인 사용자의 청첩장을 조회한다. */
    @Operation(summary = "내 청첩장 조회")
    @GetMapping("/me")
    public ApiResponse<InvitationResponse> getMine(
            @AuthenticationPrincipal Long userId) {

        return ApiResponse.ok(
                invitationService.getMine(userId)
        );
    }

    /** 로그인 사용자의 청첩장을 수정한다. */
    @Operation(summary = "청첩장 수정")
    @PatchMapping("/me")
    public ApiResponse<InvitationResponse> update(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody InvitationUpdateRequest request) {

        return ApiResponse.ok(
                invitationService.update(userId, request)
        );
    }

    /** 작성 중인 청첩장을 발행한다. */
    @Operation(summary = "청첩장 발행")
    @PatchMapping("/me/publish")
    public ApiResponse<InvitationResponse> publish(
            @AuthenticationPrincipal Long userId) {

        return ApiResponse.ok(
                invitationService.publish(userId)
        );
    }
}