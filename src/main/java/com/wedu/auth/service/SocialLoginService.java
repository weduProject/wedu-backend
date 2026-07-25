package com.wedu.auth.service;

import com.wedu.auth.dto.SocialLoginResult;
import com.wedu.global.security.jwt.JwtTokenProvider;
import com.wedu.global.security.oauth.OAuth2UserInfo;
import com.wedu.user.domain.Nickname;
import com.wedu.user.domain.User;
import com.wedu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 소셜 로그인 유스케이스: (provider, socialId) 로 조회하고 없으면 가입한 뒤 JWT 를 발급한다.
 */
@Service
@RequiredArgsConstructor
public class SocialLoginService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final SocialUserRegistrar socialUserRegistrar;

    @Transactional(readOnly = true)
    public SocialLoginResult loginOrRegister(OAuth2UserInfo info) {
        User user = userRepository
                .findByProviderAndSocialId(info.provider(), info.socialId())
                .orElseGet(() -> registerOrGetExisting(info));

        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        return SocialLoginResult.from(user, accessToken);
    }

    private User registerOrGetExisting(OAuth2UserInfo info) {
        try {
            return socialUserRegistrar.register(info);
        } catch (DataIntegrityViolationException ex) {
            return userRepository
                    .findByProviderAndSocialId(info.provider(), info.socialId())
                    .orElseThrow(() -> ex);
        }
    }
}
