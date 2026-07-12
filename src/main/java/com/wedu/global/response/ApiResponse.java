package com.wedu.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 모든 REST 응답의 공통 봉투(envelope).
 *
 * <p>성공은 {@link #ok(Object)}, 실패는 {@link #fail(String, String)} 로 생성한다.
 * {@code data} 와 {@code error} 중 하나만 채워진다(null 필드는 직렬화에서 제외).
 *
 * @param <T> 성공 시 페이로드 타입
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, T data, ErrorBody error) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null, null);
    }

    public static ApiResponse<Void> fail(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorBody(code, message));
    }

    /** 실패 응답 본문. {@code code} 는 클라이언트 분기용, {@code message} 는 사람이 읽는 설명. */
    public record ErrorBody(String code, String message) {
    }
}
