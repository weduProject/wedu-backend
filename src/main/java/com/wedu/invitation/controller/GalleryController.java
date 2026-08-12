package com.wedu.invitation.controller;

import com.wedu.global.response.ApiResponse;
import com.wedu.invitation.dto.GalleryImageCreateRequest;
import com.wedu.invitation.dto.GalleryImageResponse;
import com.wedu.invitation.service.GalleryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 모바일 청첩장 갤러리 이미지 등록·조회 HTTP 요청을 처리한다. */
@Tag(name = "Invitation Gallery", description = "모바일 청첩장 갤러리")
@RestController
@RequestMapping("/api/invitations/me/gallery")
@RequiredArgsConstructor
public class GalleryController {

    private final GalleryService galleryService;

    /** 갤러리 이미지를 추가한다. */
    @Operation(summary = "청첩장 갤러리 이미지 추가")
    @PostMapping
    public ApiResponse<GalleryImageResponse> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody GalleryImageCreateRequest request) {

        return ApiResponse.ok(
                galleryService.create(userId, request)
        );
    }

    /** 갤러리 이미지를 순서대로 조회한다. */
    @Operation(summary = "청첩장 갤러리 조회")
    @GetMapping
    public ApiResponse<List<GalleryImageResponse>> getMine(
            @AuthenticationPrincipal Long userId) {

        return ApiResponse.ok(
                galleryService.getMine(userId)
        );
    }
}