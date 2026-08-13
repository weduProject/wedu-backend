package com.wedu.community.controller;

import com.wedu.community.domain.CommunityPostSort;
import com.wedu.community.domain.PostTheme;
import com.wedu.community.dto.CommunityPostCreateRequest;
import com.wedu.community.dto.CommunityPostDetailResponse;
import com.wedu.community.dto.CommunityPostPageResponse;
import com.wedu.community.dto.CommunityPostUpdateRequest;
import com.wedu.community.service.CommunityPostService;
import com.wedu.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Hidden;
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

/** 인증 사용자의 커뮤니티 게시글 HTTP 요청을 처리한다. */
@Hidden
@Tag(name = "Community Post", description = "커뮤니티 게시글 작성·조회·검색·수정·삭제")
@RestController
@RequestMapping("/api/community/posts")
@RequiredArgsConstructor
public class CommunityPostController {

    private final CommunityPostService communityPostService;

    /** 커뮤니티 게시글을 생성한다. */
    @Operation(summary = "커뮤니티 게시글 생성")
    @PostMapping
    public ApiResponse<CommunityPostDetailResponse> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CommunityPostCreateRequest request) {
        return ApiResponse.ok(communityPostService.create(userId, request));
    }

    /** 테마·키워드와 정렬 기준으로 게시글 목록을 조회한다. */
    @Operation(summary = "커뮤니티 게시글 목록 조회", description = "제목과 본문을 부분 일치 검색한다.")
    @GetMapping
    public ApiResponse<CommunityPostPageResponse> search(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "게시글 테마. 미지정 시 전체")
            @RequestParam(required = false) PostTheme theme,
            @Parameter(description = "제목·본문 검색어")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "정렬 기준(LATEST: 최신순, MOST_LIKED: 좋아요순)")
            @RequestParam(defaultValue = "LATEST") CommunityPostSort sort,
            @Parameter(description = "0부터 시작하는 페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "페이지 크기(1~50)", example = "20")
            @RequestParam(defaultValue = "20") Integer size) {
        return ApiResponse.ok(
                communityPostService.search(userId, theme, keyword, sort, page, size));
    }

    /** 커뮤니티 게시글 상세를 조회한다. */
    @Operation(summary = "커뮤니티 게시글 상세 조회")
    @GetMapping("/{postId}")
    public ApiResponse<CommunityPostDetailResponse> getDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId) {
        return ApiResponse.ok(communityPostService.getDetail(userId, postId));
    }

    /** 작성자가 게시글을 수정한다. */
    @Operation(summary = "커뮤니티 게시글 수정")
    @PutMapping("/{postId}")
    public ApiResponse<CommunityPostDetailResponse> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId,
            @Valid @RequestBody CommunityPostUpdateRequest request) {
        return ApiResponse.ok(communityPostService.update(userId, postId, request));
    }

    /** 작성자가 게시글을 삭제한다. */
    @Operation(summary = "커뮤니티 게시글 삭제")
    @DeleteMapping("/{postId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId) {
        communityPostService.delete(userId, postId);
        return ApiResponse.ok();
    }
}
