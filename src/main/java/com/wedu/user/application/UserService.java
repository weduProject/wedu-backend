package com.wedu.user.application;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.user.domain.Nickname;
import com.wedu.user.domain.User;
import com.wedu.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * user 애플리케이션 서비스(유스케이스 오케스트레이션).
 *
 * <p>계층 책임: 트랜잭션 경계를 긋고, 애그리게이트를 조회해 도메인 메서드를 호출하고, 결과를 경계 타입으로
 * 돌려준다. 비즈니스 규칙 자체는 {@link User} 애그리게이트가 갖는다(서비스는 얇게 유지).
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /** 마이페이지(018): 내 프로필 조회. */
    @Transactional(readOnly = true)
    public UserProfileResult getProfile(Long userId) {
        User user = findByIdOrThrow(userId);
        return UserProfileResult.from(user);
    }

    /** 온보딩(002): 초기 설정 완료 처리. */
    @Transactional
    public void completeOnboarding(Long userId) {
        User user = findByIdOrThrow(userId);
        user.completeOnboarding();
    }

    /** 마이페이지(018): 프로필 수정. */
    @Transactional
    public UserProfileResult updateProfile(Long userId, String nickname, String profileImageUrl) {
        User user = findByIdOrThrow(userId);
        user.updateProfile(new Nickname(nickname), profileImageUrl);
        return UserProfileResult.from(user);
    }

    private User findByIdOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
