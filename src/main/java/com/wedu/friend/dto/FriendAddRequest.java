package com.wedu.friend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** 이메일로 상대 사용자를 찾아 친구를 추가하는 요청. */
public record FriendAddRequest(@NotBlank @Email String email) {
}
