package com.wedu.auth.service;

import com.wedu.auth.dto.SocialLoginResult;
import com.wedu.global.security.jwt.JwtTokenProvider;
import com.wedu.global.security.oauth.OAuth2UserInfo;
import com.wedu.user.domain.Nickname;
import com.wedu.user.domain.User;
import com.wedu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public SocialLoginResult loginOrRegister(OAuth2UserInfo info) {
        User user = userRepository
                .findByProviderAndSocialId(info.provider(), info.socialId())
                .orElseGet(() -> userRepository.save(User.register(
                        info.provider(),
                        info.socialId(),
                        info.email(),
                        new Nickname(info.nickname()),
                        info.profileImageUrl())));

        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        return SocialLoginResult.of(
                accessToken,
                user.getId(),
                user.getEmail(),
                user.getNickname().getValue(),
                user.isOnboardingCompleted());
    }
}
