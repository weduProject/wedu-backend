package com.wedu.global.error;

import lombok.Getter;

/**
 * 도메인 규칙 위반을 표현하는 공통 예외.
 *
 * <p>도메인/애플리케이션 계층은 이 예외만 던지고, HTTP 변환은
 * {@link GlobalExceptionHandler} 가 {@link ErrorCode} 를 보고 일괄 처리한다.
 * 계층이 HTTP 를 알 필요가 없도록 하기 위함(관심사 분리).
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String detail) {
        super(detail);
        this.errorCode = errorCode;
    }
}
