package com.wedu.community.service;

import com.wedu.community.domain.CommunityCommentLike;
import com.wedu.community.domain.CommunityPostLike;
import com.wedu.community.dto.CommunityLikeResponse;
import com.wedu.community.repository.CommunityCommentLikeRepository;
import com.wedu.community.repository.CommunityCommentRepository;
import com.wedu.community.repository.CommunityPostLikeRepository;
import com.wedu.community.repository.CommunityPostRepository;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 커뮤니티 게시글과 댓글·답글의 좋아요 유스케이스를 처리한다. */
@Service
@RequiredArgsConstructor
public class CommunityLikeService {

    private final CommunityPostRepository postRepository;
    private final CommunityCommentRepository commentRepository;
    private final CommunityPostLikeRepository postLikeRepository;
    private final CommunityCommentLikeRepository commentLikeRepository;

    /** 게시글 좋아요를 멱등하게 등록한다. */
    @Transactional
    public CommunityLikeResponse likePost(Long userId, Long postId) {
        validateUserId(userId);
        lockPost(postId);
        if (!postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
            postLikeRepository.save(CommunityPostLike.create(postId, userId));
        }
        return postResponse(postId, true);
    }

    /** 게시글 좋아요를 멱등하게 취소한다. */
    @Transactional
    public CommunityLikeResponse unlikePost(Long userId, Long postId) {
        validateUserId(userId);
        lockPost(postId);
        postLikeRepository.deleteByPostIdAndUserId(postId, userId);
        return postResponse(postId, false);
    }

    /** 댓글 또는 답글 좋아요를 멱등하게 등록한다. */
    @Transactional
    public CommunityLikeResponse likeComment(Long userId, Long commentId) {
        validateUserId(userId);
        lockComment(commentId);
        if (!commentLikeRepository.existsByCommentIdAndUserId(commentId, userId)) {
            commentLikeRepository.save(CommunityCommentLike.create(commentId, userId));
        }
        return commentResponse(commentId, true);
    }

    /** 댓글 또는 답글 좋아요를 멱등하게 취소한다. */
    @Transactional
    public CommunityLikeResponse unlikeComment(Long userId, Long commentId) {
        validateUserId(userId);
        lockComment(commentId);
        commentLikeRepository.deleteByCommentIdAndUserId(commentId, userId);
        return commentResponse(commentId, false);
    }

    private CommunityLikeResponse postResponse(Long postId, boolean likedByMe) {
        return new CommunityLikeResponse(
                postId, postLikeRepository.countByPostId(postId), likedByMe);
    }

    private CommunityLikeResponse commentResponse(Long commentId, boolean likedByMe) {
        return new CommunityLikeResponse(
                commentId, commentLikeRepository.countByCommentId(commentId), likedByMe);
    }

    private void lockPost(Long postId) {
        validatePositiveId(postId, "게시글 식별자");
        postRepository.findByIdForUpdate(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMUNITY_POST_NOT_FOUND));
    }

    private void lockComment(Long commentId) {
        validatePositiveId(commentId, "댓글 식별자");
        commentRepository.findByIdForUpdate(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMUNITY_COMMENT_NOT_FOUND));
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
}
