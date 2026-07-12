package com.wedu.user.domain;

import java.util.Optional;

/**
 * user 애그리게이트의 영속성 포트(Repository, Evans DDD 6장).
 *
 * <p>도메인 계층에 인터페이스만 두고 구현은 infrastructure 계층에 둔다(의존성 역전 — 도메인이 JPA 를
 * 알지 않게 한다). 이 인터페이스는 순수 도메인 타입만 다룬다.
 */
public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByProviderAndSocialId(SocialProvider provider, String socialId);
}
