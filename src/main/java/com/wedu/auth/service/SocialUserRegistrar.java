package com.wedu.auth.service;

import com.wedu.global.security.oauth.OAuth2UserInfo;
import com.wedu.user.domain.Nickname;
import com.wedu.user.domain.User;
import com.wedu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 소셜 신규 가입을 독립 트랜잭션으로 수행한다.
 *
 * <p>동시 최초 로그인 시 unique 제약 충돌이 나도 바깥 트랜잭션을 오염시키지 않기 위함이다.
 */
@Component
@RequiredArgsConstructor
class SocialUserRegistrar {

    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public User register(OAuth2UserInfo info) {
        return userRepository.save(User.register(
                info.provider(),
                info.socialId(),
                info.email(),
                new Nickname(info.nickname()),
                info.profileImageUrl()));
    }
}
