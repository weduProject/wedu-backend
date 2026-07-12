package com.wedu.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 도메인/공통 에러의 단일 정의 지점.
 *
 * <p>새 에러는 도메인 접두어(USER_, PRODUCT_ ...)를 붙여 여기 추가한다.
 * {@code code} 는 클라이언트 분기용 안정 식별자이므로 한 번 정하면 바꾸지 않는다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // --- 공통 ---
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_400", "입력값이 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_401", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_403", "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_404", "요청한 리소스를 찾을 수 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 오류가 발생했습니다."),

    // --- user 도메인 ---
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404", "사용자를 찾을 수 없습니다."),
    USER_ALREADY_ONBOARDED(HttpStatus.CONFLICT, "USER_409", "이미 온보딩을 완료한 사용자입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
