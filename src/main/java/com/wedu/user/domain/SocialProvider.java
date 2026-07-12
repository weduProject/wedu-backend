package com.wedu.user.domain;

/**
 * 소셜 로그인 제공자. WEDU 는 자체 비밀번호를 저장하지 않고 소셜 인증만 사용한다(README 정책).
 */
public enum SocialProvider {
    KAKAO,
    NAVER,
    GOOGLE,
}
