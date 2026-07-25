package com.wedu.auth.service;

import com.wedu.auth.dto.SocialLoginResult;
import com.wedu.global.security.oauth.OAuthLoginCodeStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 소셜 로그인 일회용 코드 → JWT 교환 유스케이스.
 */
@Service
@RequiredArgsConstructor
public class OAuthTokenExchangeService {

    private final OAuthLoginCodeStore oAuthLoginCodeStore;

    public SocialLoginResult exchange(String code) {
        return oAuthLoginCodeStore.consume(code);
    }
}
