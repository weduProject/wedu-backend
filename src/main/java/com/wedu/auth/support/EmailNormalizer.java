package com.wedu.auth.support;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import java.util.Locale;
import org.springframework.util.StringUtils;

/** 인증 흐름에서 이메일 식별자를 비교·저장하기 전 같은 형태로 맞춘다. */
public final class EmailNormalizer {

    private EmailNormalizer() {}

    public static String normalize(String email) {
        if (!StringUtils.hasText(email)) {
            throw new BusinessException(ErrorCode.AUTH_EMAIL_REQUIRED);
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
