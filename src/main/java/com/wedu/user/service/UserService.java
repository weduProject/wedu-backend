package com.wedu.user.service;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.user.domain.Nickname;
import com.wedu.user.domain.User;
import com.wedu.user.dto.UserProfileResponse;
import com.wedu.user.dto.UserPublicProfileResponse;
import com.wedu.user.repository.UserRepository;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
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

    /** 다른 도메인에 공개 가능한 단일 사용자 프로필을 조회한다. */
    @Transactional(readOnly = true)
    public UserPublicProfileResponse getPublicProfile(Long userId) {
        return UserPublicProfileResponse.from(findByIdOrThrow(userId));
    }

    /** 이메일로 사용자를 찾아 공개 프로필을 조회한다(친구 추가 등 다른 도메인에서 사용). */
    @Transactional(readOnly = true)
    public UserPublicProfileResponse getPublicProfileByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserPublicProfileResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    /** 여러 작성자의 공개 프로필을 한 번에 조회해 사용자 ID로 반환한다. */
    @Transactional(readOnly = true)
    public Map<Long, UserPublicProfileResponse> getPublicProfiles(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        Set<Long> distinctUserIds = new HashSet<>(userIds);
        Map<Long, UserPublicProfileResponse> profiles = userRepository.findAllById(distinctUserIds).stream()
                .map(UserPublicProfileResponse::from)
                .collect(Collectors.toUnmodifiableMap(
                        UserPublicProfileResponse::userId,
                        Function.identity()));
        if (profiles.size() != distinctUserIds.size()) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return profiles;
    }

    /**
     * 온보딩(002): 초기 설정 완료 처리.
     *
     * <p>완료 플래그만 켠다. 닉네임·프로필 이미지는 {@link #updateProfile}({@code PATCH /api/users/me})로
     * 따로 바꾼다.
     */
    @Transactional
    public UserProfileResponse completeOnboarding(Long userId) {
        User user = findByIdOrThrow(userId);
        user.completeOnboarding();
        return UserProfileResponse.from(user);
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
