package com.wedu.friend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.wedu.friend.repository.FriendshipRepository;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FriendAccessServiceTest {

    @Mock
    private FriendshipRepository friendshipRepository;

    private FriendAccessService friendAccessService;

    @BeforeEach
    void setUp() {
        friendAccessService = new FriendAccessService(friendshipRepository);
    }

    @Test
    @DisplayName("본인 소유 리소스는 항상 편집 가능하다")
    void allowSelf() {
        assertThatCode(() -> friendAccessService.assertEditable(1L, 1L)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("친구 관계면 편집 가능하다")
    void allowFriend() {
        when(friendshipRepository.existsByUserIdAndFriendUserId(1L, 2L)).thenReturn(true);

        assertThatCode(() -> friendAccessService.assertEditable(1L, 2L)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("친구가 아니면 편집할 수 없다")
    void rejectNonFriend() {
        when(friendshipRepository.existsByUserIdAndFriendUserId(1L, 2L)).thenReturn(false);

        assertThatThrownBy(() -> friendAccessService.assertEditable(1L, 2L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FRIEND_ACCESS_FORBIDDEN));
    }
}
