package com.wedu.friend.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FriendshipTest {

    @Test
    @DisplayName("두 사용자 사이의 친구 관계를 생성한다")
    void create() {
        Friendship friendship = Friendship.create(1L, 2L);

        assertThat(friendship.getUserId()).isEqualTo(1L);
        assertThat(friendship.getFriendUserId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("자기 자신은 친구로 추가할 수 없다")
    void rejectSelfFriend() {
        assertThatThrownBy(() -> Friendship.create(1L, 1L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FRIEND_SELF_NOT_ALLOWED));
    }

    @Test
    @DisplayName("사용자 식별자 없이는 생성할 수 없다")
    void rejectMissingUserId() {
        assertThatThrownBy(() -> Friendship.create(null, 2L)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> Friendship.create(1L, null)).isInstanceOf(BusinessException.class);
    }
}
