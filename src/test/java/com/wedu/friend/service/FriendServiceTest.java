package com.wedu.friend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wedu.friend.domain.Friendship;
import com.wedu.friend.repository.FriendshipRepository;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.user.dto.UserPublicProfileResponse;
import com.wedu.user.service.UserService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FriendServiceTest {

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private UserService userService;

    private FriendService friendService;

    @BeforeEach
    void setUp() {
        friendService = new FriendService(friendshipRepository, userService);
    }

    @Test
    @DisplayName("이메일로 상대를 찾아 양방향 친구 관계를 추가한다")
    void addFriend() {
        UserPublicProfileResponse friendProfile = new UserPublicProfileResponse(2L, "상대닉네임", null);
        when(userService.getPublicProfileByEmail("friend@wedu.com")).thenReturn(friendProfile);
        when(friendshipRepository.existsByUserIdAndFriendUserId(1L, 2L)).thenReturn(false);

        UserPublicProfileResponse result = friendService.addFriend(1L, "friend@wedu.com");

        assertThat(result.userId()).isEqualTo(2L);
        verify(friendshipRepository).saveAndFlush(argThatUserIs(1L, 2L));
        verify(friendshipRepository).saveAndFlush(argThatUserIs(2L, 1L));
    }

    @Test
    @DisplayName("이미 친구인 사용자는 다시 추가할 수 없다")
    void rejectDuplicateFriend() {
        UserPublicProfileResponse friendProfile = new UserPublicProfileResponse(2L, "상대닉네임", null);
        when(userService.getPublicProfileByEmail("friend@wedu.com")).thenReturn(friendProfile);
        when(friendshipRepository.existsByUserIdAndFriendUserId(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> friendService.addFriend(1L, "friend@wedu.com"))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FRIEND_ALREADY_EXISTS));
    }

    @Test
    @DisplayName("동시 추가로 DB 유니크 제약이 충돌해도 중복 친구 예외로 변환한다")
    void convertUniqueConstraintViolationToFriendAlreadyExists() {
        UserPublicProfileResponse friendProfile = new UserPublicProfileResponse(2L, "상대닉네임", null);
        when(userService.getPublicProfileByEmail("friend@wedu.com")).thenReturn(friendProfile);
        when(friendshipRepository.existsByUserIdAndFriendUserId(1L, 2L)).thenReturn(false);
        when(friendshipRepository.saveAndFlush(argThatUserIs(1L, 2L)))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> friendService.addFriend(1L, "friend@wedu.com"))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FRIEND_ALREADY_EXISTS));
    }

    @Test
    @DisplayName("친구 관계를 양방향으로 삭제한다")
    void removeFriend() {
        Friendship forward = Friendship.create(1L, 2L);
        Friendship backward = Friendship.create(2L, 1L);
        when(friendshipRepository.findByUserIdAndFriendUserId(1L, 2L)).thenReturn(Optional.of(forward));
        when(friendshipRepository.findByUserIdAndFriendUserId(2L, 1L)).thenReturn(Optional.of(backward));

        friendService.removeFriend(1L, 2L);

        verify(friendshipRepository).delete(forward);
        verify(friendshipRepository).delete(backward);
    }

    @Test
    @DisplayName("친구가 아닌 사용자는 삭제할 수 없다")
    void rejectRemovingNonFriend() {
        when(friendshipRepository.findByUserIdAndFriendUserId(1L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> friendService.removeFriend(1L, 2L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FRIEND_NOT_FOUND));
    }

    @Test
    @DisplayName("내 친구 목록을 공개 프로필과 함께 조회한다")
    void getMyFriends() {
        Friendship friendship = Friendship.create(1L, 2L);
        when(friendshipRepository.findAllByUserId(1L)).thenReturn(List.of(friendship));
        UserPublicProfileResponse friendProfile = new UserPublicProfileResponse(2L, "상대닉네임", null);
        when(userService.getPublicProfiles(List.of(2L))).thenReturn(Map.of(2L, friendProfile));

        List<UserPublicProfileResponse> result = friendService.getMyFriends(1L);

        assertThat(result).containsExactly(friendProfile);
    }

    @Test
    @DisplayName("친구가 없으면 빈 목록을 반환한다")
    void getMyFriendsEmpty() {
        when(friendshipRepository.findAllByUserId(1L)).thenReturn(List.of());

        List<UserPublicProfileResponse> result = friendService.getMyFriends(1L);

        assertThat(result).isEmpty();
    }

    private Friendship argThatUserIs(Long userId, Long friendUserId) {
        return org.mockito.ArgumentMatchers.argThat(friendship ->
                friendship.getUserId().equals(userId) && friendship.getFriendUserId().equals(friendUserId));
    }
}
