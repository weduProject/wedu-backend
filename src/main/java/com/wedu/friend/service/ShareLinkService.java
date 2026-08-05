package com.wedu.friend.service;

import com.wedu.friend.domain.ShareLink;
import com.wedu.friend.dto.ShareLinkResponse;
import com.wedu.friend.repository.ShareLinkRepository;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 플래너 데이터 조회 전용 공유 링크(토큰) 유스케이스를 처리한다. */
@Service
@RequiredArgsConstructor
public class ShareLinkService {

    private final ShareLinkRepository shareLinkRepository;

    /** 내 공유 링크를 조회하고, 없으면 새로 발급한다. */
    @Transactional
    public ShareLinkResponse getOrIssueMyLink(Long ownerId) {
        ShareLink shareLink = shareLinkRepository.findByOwnerId(ownerId)
                .orElseGet(() -> shareLinkRepository.save(ShareLink.issue(ownerId)));
        return ShareLinkResponse.from(shareLink);
    }

    /** 기존 링크를 무효화하고 새 토큰을 발급한다. */
    @Transactional
    public ShareLinkResponse reissueMyLink(Long ownerId) {
        ShareLink shareLink = shareLinkRepository.findByOwnerId(ownerId)
                .orElseGet(() -> ShareLink.issue(ownerId));
        shareLink.reissue();
        return ShareLinkResponse.from(shareLinkRepository.save(shareLink));
    }

    /** 토큰으로 소유자 식별자를 찾는다. 조회 전용 접근에만 사용한다. */
    @Transactional(readOnly = true)
    public Long resolveOwnerId(String token) {
        return shareLinkRepository.findByToken(token)
                .map(ShareLink::getOwnerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHARE_LINK_NOT_FOUND));
    }
}
