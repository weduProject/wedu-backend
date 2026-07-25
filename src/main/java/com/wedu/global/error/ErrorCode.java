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
    USER_ALREADY_ONBOARDED(HttpStatus.CONFLICT, "USER_409", "이미 온보딩을 완료한 사용자입니다."),

    // --- auth 도메인 ---
    AUTH_TEMP_LOGIN_DISABLED(HttpStatus.FORBIDDEN, "AUTH_403", "임시 로그인은 현재 사용할 수 없습니다."),

    // --- planner 도메인 ---
    PLANNER_DDAY_NOT_FOUND(HttpStatus.NOT_FOUND, "PLANNER_404", "등록된 결혼식 D-day를 찾을 수 없습니다."),
    PLANNER_DDAY_ALREADY_EXISTS(HttpStatus.CONFLICT, "PLANNER_409", "결혼식 D-day가 이미 등록되어 있습니다."),
    PLANNER_CALENDAR_EVENT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "PLANNER_CALENDAR_404",
            "캘린더 일정을 찾을 수 없습니다."),
    PLANNER_CHECKLIST_ITEM_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "PLANNER_CHECKLIST_404",
            "체크리스트 항목을 찾을 수 없습니다."),
    PLANNER_BUDGET_ITEM_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "PLANNER_BUDGET_404",
            "예산 항목을 찾을 수 없습니다."),

    // --- product 도메인 ---
    PRODUCT_INVALID_PRICE(HttpStatus.BAD_REQUEST, "PRODUCT_400", "상품 가격은 0 이상이어야 합니다."),
    PRODUCT_INVALID_PRICE_RANGE(
            HttpStatus.BAD_REQUEST,
            "PRODUCT_400_RANGE",
            "최소 가격은 최대 가격보다 클 수 없습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_404", "상품을 찾을 수 없습니다."),
    PRODUCT_INVALID_NAME(HttpStatus.BAD_REQUEST, "PRODUCT_400_NAME", "상품명이 올바르지 않습니다."),
    PRODUCT_INVALID_SOURCE(
            HttpStatus.BAD_REQUEST,
            "PRODUCT_400_SOURCE",
            "수집 출처 정보가 올바르지 않습니다."),
    PRODUCT_INVALID_RANK(HttpStatus.BAD_REQUEST, "PRODUCT_400_RANK", "순위는 1 이상이어야 합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
