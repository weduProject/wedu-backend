package com.wedu.global.security.oauth;

import com.wedu.user.domain.SocialProvider;

/**
 * 소셜 제공자에서 추출한 최소 프로필. 회원 조회/가입 입력으로만 쓰인다.
 */
public record OAuth2UserInfo(
        SocialProvider provider,
        String socialId,
        String email,
        String nickname,
        String profileImageUrl) {
}
