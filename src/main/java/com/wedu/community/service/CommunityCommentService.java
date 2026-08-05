package com.wedu.community.service;

import com.wedu.community.domain.CommunityComment;
import com.wedu.community.domain.CommunityPost;
import com.wedu.community.dto.CommunityCommentCreateRequest;
import com.wedu.community.dto.CommunityCommentPageResponse;
import com.wedu.community.dto.CommunityCommentResponse;
import com.wedu.community.dto.CommunityCommentSummaryResponse;
import com.wedu.community.dto.CommunityCommentUpdateRequest;
import com.wedu.community.dto.CommunityReplyPageResponse;
import com.wedu.community.repository.CommunityCommentRepository;
import com.wedu.community.repository.CommunityPostRepository;
import com.wedu.community.repository.CommunityReplyCountProjection;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.user.dto.UserPublicProfileResponse;
import com.wedu.user.service.UserService;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 커뮤니티 댓글과 1단계 답글 유스케이스를 처리한다. */
@Service
@RequiredArgsConstructor
public class CommunityCommentService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

    private final CommunityCommentRepository commentRepository;
    private final CommunityPostRepository postRepository;
    private final UserService userService;

    /** 게시글에 최상위 댓글을 생성한다. */
    @Transactional
    public CommunityCommentResponse create(
            Long userId, Long postId, CommunityCommentCreateRequest request) {
        validateUserId(userId);
        validateCreateRequest(request);
        CommunityPost post = findPost(postId);
        CommunityComment comment = CommunityComment.createComment(
                postId, userId, request.content(), request.anonymous());
        commentRepository.save(comment);
        return toResponse(comment, userId, post.getAuthorId());
    }

    /** 최상위 댓글에 1단계 답글을 생성한다. */
    @Transactional
    public CommunityCommentResponse createReply(
            Long userId,
            Long postId,
            Long parentCommentId,
            CommunityCommentCreateRequest request) {
        validateUserId(userId);
        validateCreateRequest(request);
        CommunityPost post = findPost(postId);
        CommunityComment parent = findComment(parentCommentId);
        if (!parent.getPostId().equals(postId)) {
            throw new BusinessException(ErrorCode.COMMUNITY_COMMENT_NOT_FOUND);
        }
        if (parent.isReply()) {
            throw new BusinessException(ErrorCode.COMMUNITY_COMMENT_REPLY_NOT_ALLOWED);
        }
        CommunityComment reply = CommunityComment.createReply(
                postId, userId, parent.getId(), request.content(), request.anonymous());
        commentRepository.save(reply);
        return toResponse(reply, userId, post.getAuthorId());
    }

    /** 최상위 댓글과 각 댓글의 답글 수를 페이징 조회한다. */
    @Transactional(readOnly = true)
    public CommunityCommentPageResponse getComments(
            Long userId, Long postId, Integer page, Integer size) {
        validateUserId(userId);
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);
        CommunityPost post = findPost(postId);
        Page<CommunityComment> parents =
                commentRepository.findByPostIdAndParentIdIsNullOrderByCreatedAtAscIdAsc(
                        postId, PageRequest.of(normalizedPage, normalizedSize));
        Map<Long, UserPublicProfileResponse> profiles = loadPublicProfiles(parents.getContent());
        Map<Long, Long> replyCounts = loadReplyCounts(parents.getContent());
        List<CommunityCommentSummaryResponse> comments = parents.getContent().stream()
                .map(parent -> CommunityCommentSummaryResponse.from(
                        parent,
                        userId,
                        post.getAuthorId(),
                        profiles.get(parent.getAuthorId()),
                        replyCounts.getOrDefault(parent.getId(), 0L)))
                .toList();
        return new CommunityCommentPageResponse(
                comments,
                parents.getNumber(),
                parents.getSize(),
                parents.getTotalElements(),
                parents.getTotalPages(),
                parents.hasNext());
    }

    /** 특정 최상위 댓글의 1단계 답글을 페이징 조회한다. */
    @Transactional(readOnly = true)
    public CommunityReplyPageResponse getReplies(
            Long userId, Long parentCommentId, Integer page, Integer size) {
        validateUserId(userId);
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);
        CommunityComment parent = findComment(parentCommentId);
        if (parent.isReply()) {
            throw new BusinessException(ErrorCode.COMMUNITY_COMMENT_PARENT_REQUIRED);
        }
        CommunityPost post = findPost(parent.getPostId());
        Page<CommunityComment> replies =
                commentRepository.findByParentIdOrderByCreatedAtAscIdAsc(
                        parent.getId(), PageRequest.of(normalizedPage, normalizedSize));
        Map<Long, UserPublicProfileResponse> profiles = loadPublicProfiles(replies.getContent());
        List<CommunityCommentResponse> responses = replies.getContent().stream()
                .map(reply -> CommunityCommentResponse.from(
                        reply,
                        userId,
                        post.getAuthorId(),
                        profiles.get(reply.getAuthorId())))
                .toList();
        return new CommunityReplyPageResponse(
                responses,
                replies.getNumber(),
                replies.getSize(),
                replies.getTotalElements(),
                replies.getTotalPages(),
                replies.hasNext());
    }

    /** 작성자가 댓글 또는 답글의 내용과 익명 여부를 수정한다. */
    @Transactional
    public CommunityCommentResponse update(
            Long userId, Long commentId, CommunityCommentUpdateRequest request) {
        validateUserId(userId);
        validateUpdateRequest(request);
        CommunityComment comment = findOwnedComment(userId, commentId);
        CommunityPost post = findPost(comment.getPostId());
        comment.update(request.content(), request.anonymous());
        return toResponse(comment, userId, post.getAuthorId());
    }

    /** 작성자가 댓글을 삭제하며 최상위 댓글이면 답글도 함께 삭제한다. */
    @Transactional
    public void delete(Long userId, Long commentId) {
        validateUserId(userId);
        CommunityComment comment = findOwnedComment(userId, commentId);
        if (!comment.isReply()) {
            commentRepository.deleteByParentId(comment.getId());
        }
        commentRepository.delete(comment);
    }

    private CommunityCommentResponse toResponse(
            CommunityComment comment,
            Long viewerId,
            Long postAuthorId) {
        UserPublicProfileResponse profile = comment.isAnonymous()
                ? null
                : userService.getPublicProfile(comment.getAuthorId());
        return CommunityCommentResponse.from(comment, viewerId, postAuthorId, profile);
    }

    private Map<Long, UserPublicProfileResponse> loadPublicProfiles(
            Collection<CommunityComment> comments) {
        Set<Long> authorIds = new LinkedHashSet<>(comments.stream()
                .filter(comment -> !comment.isAnonymous())
                .map(CommunityComment::getAuthorId)
                .toList());
        return authorIds.isEmpty() ? Map.of() : userService.getPublicProfiles(authorIds);
    }

    private Map<Long, Long> loadReplyCounts(List<CommunityComment> parents) {
        List<Long> parentIds = parents.stream().map(CommunityComment::getId).toList();
        if (parentIds.isEmpty()) {
            return Map.of();
        }
        return commentRepository.countRepliesByParentIds(parentIds).stream()
                .collect(Collectors.toMap(
                        CommunityReplyCountProjection::getParentId,
                        CommunityReplyCountProjection::getReplyCount));
    }

    private CommunityComment findOwnedComment(Long userId, Long commentId) {
        CommunityComment comment = findComment(commentId);
        if (!comment.isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.COMMUNITY_COMMENT_FORBIDDEN);
        }
        return comment;
    }

    private CommunityComment findComment(Long commentId) {
        validatePositiveId(commentId, "댓글 식별자");
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMUNITY_COMMENT_NOT_FOUND));
    }

    private CommunityPost findPost(Long postId) {
        validatePositiveId(postId, "게시글 식별자");
        return postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMUNITY_POST_NOT_FOUND));
    }

    private void validateCreateRequest(CommunityCommentCreateRequest request) {
        if (request == null || request.anonymous() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "댓글 생성 요청은 필수입니다.");
        }
    }

    private void validateUpdateRequest(CommunityCommentUpdateRequest request) {
        if (request == null || request.anonymous() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "댓글 수정 요청은 필수입니다.");
        }
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }

    private void validatePositiveId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, fieldName + "는 양수여야 합니다.");
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
}
