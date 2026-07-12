package com.wedu.user.infrastructure;

import com.wedu.user.domain.SocialProvider;
import com.wedu.user.domain.User;
import com.wedu.user.domain.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * {@link UserRepository} 포트의 JPA 어댑터. Spring Data 인터페이스를 감싸 도메인 포트로 노출한다.
 *
 * <p>지금은 위임뿐이지만, 캐싱·복수 저장소 조합·매핑 변환 같은 기술 관심사가 생기면 이 지점에 격리한다.
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpaRepository;

    @Override
    public User save(User user) {
        return jpaRepository.save(user);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<User> findByProviderAndSocialId(SocialProvider provider, String socialId) {
        return jpaRepository.findByProviderAndSocialId(provider, socialId);
    }
}
