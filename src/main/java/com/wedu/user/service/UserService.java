package com.wedu.user.service;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.user.domain.Nickname;
import com.wedu.user.domain.User;
import com.wedu.user.dto.UserProfileResponse;
import com.wedu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * user 서비스. 유스케이스 하나를 public 메서드 하나로 두고, 트랜잭션 경계를 긋는다.
 *
 * <p>엔티티를 조회해 도메인 메서드를 호출하고 응답 DTO 로 옮겨 준다. 비즈니스 규칙 자체는
 * {@link User} 엔티티가 갖는다(서비스는 얇게 유지).
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /** 마이페이지(018): 내 프로필 조회. */
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        return UserProfileResponse.from(findByIdOrThrow(userId));
    }

    /** 온보딩(002): 초기 설정 완료 처리. */
    @Transactional
    public void completeOnboarding(Long userId) {
        User user = findByIdOrThrow(userId);
        user.completeOnboarding();
    }

    /** 마이페이지(018): 프로필 수정. */
    @Transactional
    public UserProfileResponse updateProfile(Long userId, String nickname, String profileImageUrl) {
        User user = findByIdOrThrow(userId);
        user.updateProfile(new Nickname(nickname), profileImageUrl);
        return UserProfileResponse.from(user);
    }

    private User findByIdOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
