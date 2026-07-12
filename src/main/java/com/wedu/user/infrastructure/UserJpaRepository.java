package com.wedu.user.infrastructure;

import com.wedu.user.domain.SocialProvider;
import com.wedu.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA 기술 인터페이스. infrastructure 내부 구현 세부사항이며,
 * 애플리케이션/도메인 계층은 {@link com.wedu.user.domain.UserRepository} 포트만 참조한다.
 */
interface UserJpaRepository extends JpaRepository<User, Long> {

    Optional<User> findByProviderAndSocialId(SocialProvider provider, String socialId);
}
