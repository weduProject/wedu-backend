package com.wedu.user.repository;

import com.wedu.user.domain.SocialProvider;
import com.wedu.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * user DB 접근. {@link JpaRepository} 를 상속하면 {@code save}·{@code findById} 등 기본 CRUD 는
 * 자동으로 제공된다. 필요한 조회는 메서드 이름으로 선언하면 Spring Data 가 쿼리를 만들어 준다.
 *
 * <p>복잡한 쿼리가 필요하면 {@code @Query}(JPQL) 를 쓴다. 네이티브 SQL 은 쓰지 않는다.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByProviderAndSocialId(SocialProvider provider, String socialId);
}
