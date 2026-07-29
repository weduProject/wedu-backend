package com.wedu.user.domain;

/**
 * 사용자 인증 제공자. 소셜 로그인과 이메일 기반 자체 로그인을 같은 사용자 모델에서 구분한다.
 */
public enum SocialProvider {
    LOCAL,
    KAKAO,
    GOOGLE,
}
