package com.wedu.planner.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/** 결혼식 D-day 생성·수정 요청. */
public record DDayRequest(
        @NotNull(message = "결혼식 날짜는 필수입니다.") LocalDate weddingDate) {
}
