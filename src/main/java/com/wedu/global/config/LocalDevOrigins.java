package com.wedu.global.config;

import java.util.Set;

/**
 * 공유 dev 서버 없이 각자 로컬에서 개발하는 팀 상황을 감안해, CORS·OAuth 로그인 리다이렉트
 * 양쪽에서 공통으로 허용하는 로컬 개발 서버 origin.
 *
 * <p>{@code CORS_ALLOWED_ORIGINS} 시크릿(운영 도메인만 등록)과는 별개로 코드에 고정해둔다 —
 * 값 자체가 비밀도 아니고, 팀원 로컬 개발 편의를 위한 것이라 서버 설정을 건드릴 필요가 없다.
 */
public final class LocalDevOrigins {

    public static final Set<String> ORIGINS = Set.of(
            "http://localhost:5173",
            "http://localhost:3000");

    private LocalDevOrigins() {
    }
}
