package com.wedu.friend.service;

import com.wedu.friend.repository.FriendshipRepository;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 플래너(D-day/캘린더/체크리스트/예산) 공동 편집 권한을 검사한다.
 *
 * <p>본인이거나 서로 친구인 경우에만 조회·편집을 허용한다("친구=편집가능"). 링크로만 아는 사용자는
 * {@link ShareLinkService} 를 통한 조회 전용 접근만 가능하다.
 */
@Service
@RequiredArgsConstructor
public class FriendAccessService {

    private final FriendshipRepository friendshipRepository;

    /** requesterId 가 ownerId 의 플래너 데이터를 편집할 수 있는지 검사하고, 아니면 예외를 던진다. */
    @Transactional(readOnly = true)
    public void assertEditable(Long requesterId, Long ownerId) {
        if (requesterId.equals(ownerId)) {
            return;
        }
        if (!friendshipRepository.existsByUserIdAndFriendUserId(requesterId, ownerId)) {
            throw new BusinessException(ErrorCode.FRIEND_ACCESS_FORBIDDEN);
        }
    }
}
