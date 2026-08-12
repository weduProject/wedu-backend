package com.wedu.auth.service;

import com.wedu.auth.dto.SocialLoginResult;
import com.wedu.auth.support.EmailNormalizer;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.global.security.jwt.JwtTokenProvider;
import com.wedu.global.security.oauth.OAuth2UserInfo;
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
        OAuth2UserInfo normalizedInfo = normalize(info);
        User user = userRepository
                .findByProviderAndSocialId(normalizedInfo.provider(), normalizedInfo.socialId())
                .orElseGet(() -> registerOrGetExisting(normalizedInfo));

        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        return SocialLoginResult.from(user, accessToken);
    }

    private User registerOrGetExisting(OAuth2UserInfo info) {
        if (userRepository.existsByEmail(info.email())) {
            throw new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS);
        }
        try {
            return socialUserRegistrar.register(info);
        } catch (DataIntegrityViolationException ex) {
            return socialUserRegistrar
                    .findExisting(info.provider(), info.socialId())
                    .orElseThrow(() -> toDuplicateEmailOrRethrow(ex));
        }
    }

    private OAuth2UserInfo normalize(OAuth2UserInfo info) {
        return new OAuth2UserInfo(
                info.provider(),
                info.socialId(),
                EmailNormalizer.normalize(info.email()),
                info.nickname(),
                info.profileImageUrl());
    }

    private RuntimeException toDuplicateEmailOrRethrow(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause().getMessage();
        if (message != null && message.toLowerCase().contains("email")) {
            return new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS);
        }
        return ex;
    }
}
