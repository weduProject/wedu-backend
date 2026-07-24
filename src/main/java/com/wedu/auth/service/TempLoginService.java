package com.wedu.auth.service;

import com.wedu.auth.dto.TempLoginRequest;
import com.wedu.auth.dto.TempLoginResponse;
import com.wedu.global.security.jwt.JwtTokenProvider;
import com.wedu.user.domain.Nickname;
import com.wedu.user.domain.SocialProvider;
import com.wedu.user.domain.User;
import com.wedu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("!prod")
@RequiredArgsConstructor
public class TempLoginService {

    private static final SocialProvider TEMP_LOGIN_PROVIDER = SocialProvider.KAKAO;
    private static final String TEMP_SOCIAL_ID_PREFIX = "temp:";

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public TempLoginResponse login(TempLoginRequest request) {
        String email = request.email().trim();
        String socialId = TEMP_SOCIAL_ID_PREFIX + email;
        User user = userRepository.findByProviderAndSocialId(TEMP_LOGIN_PROVIDER, socialId)
                .orElseGet(() -> userRepository.save(User.register(
                        TEMP_LOGIN_PROVIDER,
                        socialId,
                        email,
                        new Nickname(request.nickname()),
                        null)));

        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        return TempLoginResponse.of(accessToken, user);
    }
}
