package com.wedu.friend.service;

import com.wedu.friend.domain.Friendship;
import com.wedu.friend.repository.FriendshipRepository;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.user.dto.UserPublicProfileResponse;
import com.wedu.user.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 친구 추가/삭제/목록 조회 유스케이스를 처리한다. */
@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendshipRepository friendshipRepository;
    private final UserService userService;

    /** 이메일로 상대 사용자를 찾아 양방향 친구 관계를 추가한다. */
    @Transactional
    public UserPublicProfileResponse addFriend(Long userId, String friendEmail) {
        UserPublicProfileResponse friendProfile = userService.getPublicProfileByEmail(friendEmail);
        Long friendUserId = friendProfile.userId();

        if (friendshipRepository.existsByUserIdAndFriendUserId(userId, friendUserId)) {
            throw new BusinessException(ErrorCode.FRIEND_ALREADY_EXISTS);
        }

        friendshipRepository.save(Friendship.create(userId, friendUserId));
        friendshipRepository.save(Friendship.create(friendUserId, userId));

        return friendProfile;
    }

    /** 친구 관계를 양방향으로 삭제한다. */
    @Transactional
    public void removeFriend(Long userId, Long friendUserId) {
        Friendship friendship = friendshipRepository.findByUserIdAndFriendUserId(userId, friendUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FRIEND_NOT_FOUND));
        friendshipRepository.delete(friendship);
        friendshipRepository.findByUserIdAndFriendUserId(friendUserId, userId)
                .ifPresent(friendshipRepository::delete);
    }

    /** 내 친구 목록을 공개 프로필 정보와 함께 조회한다. */
    @Transactional(readOnly = true)
    public List<UserPublicProfileResponse> getMyFriends(Long userId) {
        List<Long> friendUserIds = friendshipRepository.findAllByUserId(userId).stream()
                .map(Friendship::getFriendUserId)
                .toList();
        if (friendUserIds.isEmpty()) {
            return List.of();
        }
        return List.copyOf(userService.getPublicProfiles(friendUserIds).values());
    }
}
