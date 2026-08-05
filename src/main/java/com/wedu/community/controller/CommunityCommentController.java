package com.wedu.community.controller;

import com.wedu.community.dto.CommunityCommentCreateRequest;
import com.wedu.community.dto.CommunityCommentPageResponse;
import com.wedu.community.dto.CommunityCommentResponse;
import com.wedu.community.dto.CommunityCommentUpdateRequest;
import com.wedu.community.dto.CommunityReplyPageResponse;
import com.wedu.community.service.CommunityCommentService;
import com.wedu.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

/** 인증 사용자의 커뮤니티 댓글 HTTP 요청을 처리한다. */
@Tag(name = "Community Comment", description = "커뮤니티 댓글·답글 생성·조회·수정·삭제")
@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityCommentController {

    private final CommunityCommentService commentService;

    @Operation(summary = "커뮤니티 댓글 생성")
    @PostMapping("/posts/{postId}/comments")
    public ApiResponse<CommunityCommentResponse> create(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId,
            @Valid @RequestBody CommunityCommentCreateRequest request) {
        return ApiResponse.ok(commentService.create(userId, postId, request));
    }

    @Operation(summary = "커뮤니티 1단계 답글 생성")
    @PostMapping("/posts/{postId}/comments/{commentId}/replies")
    public ApiResponse<CommunityCommentResponse> createReply(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommunityCommentCreateRequest request) {
        return ApiResponse.ok(
                commentService.createReply(userId, postId, commentId, request));
    }

    @Operation(summary = "커뮤니티 댓글 목록 조회", description = "최상위 댓글과 각 댓글의 답글 수를 페이징합니다.")
    @GetMapping("/posts/{postId}/comments")
    public ApiResponse<CommunityCommentPageResponse> getComments(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId,
            @Parameter(description = "0부터 시작하는 페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "최상위 댓글 페이지 크기(1~50)", example = "20")
            @RequestParam(defaultValue = "20") Integer size) {
        return ApiResponse.ok(commentService.getComments(userId, postId, page, size));
    }

    @Operation(summary = "커뮤니티 답글 목록 조회", description = "특정 최상위 댓글의 1단계 답글을 페이징합니다.")
    @GetMapping("/comments/{commentId}/replies")
    public ApiResponse<CommunityReplyPageResponse> getReplies(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long commentId,
            @Parameter(description = "0부터 시작하는 페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "답글 페이지 크기(1~50)", example = "20")
            @RequestParam(defaultValue = "20") Integer size) {
        return ApiResponse.ok(commentService.getReplies(userId, commentId, page, size));
    }

    @Operation(summary = "커뮤니티 댓글 또는 답글 수정")
    @PutMapping("/comments/{commentId}")
    public ApiResponse<CommunityCommentResponse> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommunityCommentUpdateRequest request) {
        return ApiResponse.ok(commentService.update(userId, commentId, request));
    }

    @Operation(summary = "커뮤니티 댓글 또는 답글 삭제")
    @DeleteMapping("/comments/{commentId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long commentId) {
        commentService.delete(userId, commentId);
        return ApiResponse.ok();
    }
}
