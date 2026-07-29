package com.wedu.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 이메일 기반 회원가입 요청. 화면의 "이름"은 서비스 내부 닉네임으로 저장한다. */
public record EmailSignupRequest(
        @NotBlank(message = "이름은 필수입니다.") String name,
        @NotBlank(message = "이메일은 필수입니다.") @Email(message = "이메일 형식이 올바르지 않습니다.")
                String email,
        @NotBlank(message = "비밀번호는 필수입니다.") @Size(min = 6, message = "비밀번호는 6자 이상이어야 합니다.")
                String password,
        @NotBlank(message = "비밀번호 확인은 필수입니다.") String passwordConfirm) {}
