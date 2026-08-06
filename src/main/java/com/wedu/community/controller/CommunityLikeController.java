package com.wedu.community.controller;

import com.wedu.community.dto.CommunityLikeResponse;
import com.wedu.community.service.CommunityLikeService;
import com.wedu.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 인증 사용자의 커뮤니티 좋아요 HTTP 요청을 처리한다. */
@Tag(name = "Community Like", description = "커뮤니티 게시글·댓글·답글 좋아요 등록과 취소")
@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityLikeController {

    private final CommunityLikeService likeService;

    @Operation(summary = "커뮤니티 게시글 좋아요 등록")
    @PostMapping("/posts/{postId}/likes")
    public ApiResponse<CommunityLikeResponse> likePost(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId) {
        return ApiResponse.ok(likeService.likePost(userId, postId));
    }

    @Operation(summary = "커뮤니티 게시글 좋아요 취소")
    @DeleteMapping("/posts/{postId}/likes")
    public ApiResponse<CommunityLikeResponse> unlikePost(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId) {
        return ApiResponse.ok(likeService.unlikePost(userId, postId));
    }

    @Operation(summary = "커뮤니티 댓글 또는 답글 좋아요 등록")
    @PostMapping("/comments/{commentId}/likes")
    public ApiResponse<CommunityLikeResponse> likeComment(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long commentId) {
        return ApiResponse.ok(likeService.likeComment(userId, commentId));
    }

    @Operation(summary = "커뮤니티 댓글 또는 답글 좋아요 취소")
    @DeleteMapping("/comments/{commentId}/likes")
    public ApiResponse<CommunityLikeResponse> unlikeComment(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long commentId) {
        return ApiResponse.ok(likeService.unlikeComment(userId, commentId));
    }
}
