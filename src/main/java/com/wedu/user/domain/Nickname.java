package com.wedu.user.domain;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 닉네임 값 객체(Value Object).
 *
 * <p>식별자가 없고 값으로 동등성이 결정된다(Evans, DDD 3장). 생성 시점에 불변식(공백 불가, 길이 제한)을
 * 강제하므로, 유효하지 않은 Nickname 인스턴스는 애초에 존재할 수 없다. 한 번 만들어지면 바뀌지 않는다.
 */
@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Nickname {

    private static final int MAX_LENGTH = 20;

    @Column(name = "nickname", nullable = false, length = MAX_LENGTH)
    private String value;

    public Nickname(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "닉네임은 비어 있을 수 없습니다.");
        }
        String trimmed = value.trim();
        if (trimmed.length() > MAX_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "닉네임은 최대 " + MAX_LENGTH + "자입니다.");
        }
        this.value = trimmed;
    }
}
