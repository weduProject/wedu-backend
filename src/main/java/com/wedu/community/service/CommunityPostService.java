package com.wedu.community.service;

import com.wedu.community.domain.CommunityPost;
import com.wedu.community.domain.PostTheme;
import com.wedu.community.dto.CommunityPostCreateRequest;
import com.wedu.community.dto.CommunityPostDetailResponse;
import com.wedu.community.dto.CommunityPostPageResponse;
import com.wedu.community.dto.CommunityPostSummaryResponse;
import com.wedu.community.dto.CommunityPostUpdateRequest;
import com.wedu.community.repository.CommunityCommentCountProjection;
import com.wedu.community.repository.CommunityCommentRepository;
import com.wedu.community.repository.CommunityPostRepository;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.user.dto.UserPublicProfileResponse;
import com.wedu.user.service.UserService;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 커뮤니티 게시글 CRUD와 검색 유스케이스를 처리한다. */
@Service
@RequiredArgsConstructor
public class CommunityPostService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;
    private static final int MAX_KEYWORD_LENGTH = 100;

    private final CommunityPostRepository communityPostRepository;
    private final CommunityCommentRepository communityCommentRepository;
    private final UserService userService;

    /** 인증 사용자의 게시글을 생성한다. */
    @Transactional
    public CommunityPostDetailResponse create(Long userId, CommunityPostCreateRequest request) {
        validateUserId(userId);
        if (request == null || request.anonymous() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "게시글 생성 요청은 필수입니다.");
        }
        CommunityPost post = CommunityPost.create(
                userId,
                request.title(),
                request.content(),
                request.theme(),
                request.anonymous());
        communityPostRepository.save(post);
        return toDetail(post, userId);
    }

    /** 테마와 제목·본문 키워드로 최신 게시글을 페이징 조회한다. */
    @Transactional(readOnly = true)
    public CommunityPostPageResponse search(
            Long userId,
            PostTheme theme,
            String keyword,
            Integer page,
            Integer size) {
        validateUserId(userId);
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);
        String normalizedKeyword = normalizeKeyword(keyword);
        Page<CommunityPost> result = communityPostRepository.search(
                theme, normalizedKeyword, PageRequest.of(normalizedPage, normalizedSize));
        Map<Long, UserPublicProfileResponse> profiles = loadPublicProfiles(result.getContent());
        Map<Long, Long> commentCounts = loadCommentCounts(result.getContent());
        List<CommunityPostSummaryResponse> posts = result.getContent().stream()
                .map(post -> CommunityPostSummaryResponse.from(
                        post,
                        userId,
                        profiles.get(post.getAuthorId()),
                        commentCounts.getOrDefault(post.getId(), 0L)))
                .toList();
        return new CommunityPostPageResponse(
                posts,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext());
    }

    /** 인증 사용자가 선택한 게시글 상세를 조회한다. */
    @Transactional(readOnly = true)
    public CommunityPostDetailResponse getDetail(Long userId, Long postId) {
        validateUserId(userId);
        return toDetail(findPost(postId), userId);
    }

    /** 작성자가 게시글 전체 정보를 수정한다. */
    @Transactional
    public CommunityPostDetailResponse update(
            Long userId,
            Long postId,
            CommunityPostUpdateRequest request) {
        validateUserId(userId);
        if (request == null || request.anonymous() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "게시글 수정 요청은 필수입니다.");
        }
        CommunityPost post = findOwnedPost(userId, postId);
        post.update(
                request.title(),
                request.content(),
                request.theme(),
                request.anonymous());
        return toDetail(post, userId);
    }

    /** 작성자가 자신의 게시글을 삭제한다. */
    @Transactional
    public void delete(Long userId, Long postId) {
        validateUserId(userId);
        CommunityPost post = findOwnedPost(userId, postId);
        communityCommentRepository.deleteByPostId(postId);
        communityPostRepository.delete(post);
    }

    private CommunityPostDetailResponse toDetail(CommunityPost post, Long viewerId) {
        UserPublicProfileResponse profile = post.isAnonymous()
                ? null
                : userService.getPublicProfile(post.getAuthorId());
        return CommunityPostDetailResponse.from(
                post, viewerId, profile, communityCommentRepository.countByPostId(post.getId()));
    }

    private Map<Long, UserPublicProfileResponse> loadPublicProfiles(List<CommunityPost> posts) {
        Set<Long> authorIds = new LinkedHashSet<>(posts.stream()
                        .filter(post -> !post.isAnonymous())
                        .map(CommunityPost::getAuthorId)
                        .toList());
        return authorIds.isEmpty() ? Map.of() : userService.getPublicProfiles(authorIds);
    }

    private Map<Long, Long> loadCommentCounts(List<CommunityPost> posts) {
        List<Long> postIds = posts.stream().map(CommunityPost::getId).toList();
        if (postIds.isEmpty()) {
            return Map.of();
        }
        return communityCommentRepository.countByPostIds(postIds).stream()
                .collect(java.util.stream.Collectors.toMap(
                        CommunityCommentCountProjection::getPostId,
                        CommunityCommentCountProjection::getCommentCount));
    }

    private CommunityPost findOwnedPost(Long userId, Long postId) {
        CommunityPost post = findPost(postId);
        if (!post.isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.COMMUNITY_POST_FORBIDDEN);
        }
        return post;
    }

    private CommunityPost findPost(Long postId) {
        if (postId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "게시글 식별자는 필수입니다.");
        }
        return communityPostRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMUNITY_POST_NOT_FOUND));
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }

    private int normalizePage(Integer page) {
        int normalized = page == null ? 0 : page;
        if (normalized < 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "페이지 번호는 0 이상이어야 합니다.");
        }
        return normalized;
    }

    private int normalizeSize(Integer size) {
        int normalized = size == null ? DEFAULT_PAGE_SIZE : size;
        if (normalized < 1 || normalized > MAX_PAGE_SIZE) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "페이지 크기는 1 이상 " + MAX_PAGE_SIZE + " 이하여야 합니다.");
        }
        return normalized;
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        String normalized = keyword.trim();
        if (normalized.codePointCount(0, normalized.length()) > MAX_KEYWORD_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "검색어는 100자 이하여야 합니다.");
        }
        return normalized
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
    }
}
