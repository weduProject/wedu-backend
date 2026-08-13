package com.wedu.friend.controller;

import com.wedu.friend.dto.FriendAddRequest;
import com.wedu.friend.service.FriendService;
import com.wedu.global.response.ApiResponse;
import com.wedu.user.dto.UserPublicProfileResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 인증 사용자의 친구 추가/삭제/목록 조회 HTTP 요청을 처리한다. */
@Hidden
@Tag(name = "Friend", description = "친구 추가/삭제/목록 조회")
@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    /** 이메일로 상대 사용자를 찾아 친구로 추가한다. */
    @Operation(summary = "친구 추가")
    @PostMapping
    public ApiResponse<UserPublicProfileResponse> addFriend(
            @AuthenticationPrincipal Long userId, @Valid @RequestBody FriendAddRequest request) {
        return ApiResponse.ok(friendService.addFriend(userId, request.email()));
    }

    /** 내 친구 목록을 조회한다. */
    @Operation(summary = "내 친구 목록 조회")
    @GetMapping("/me")
    public ApiResponse<List<UserPublicProfileResponse>> getMyFriends(
            @AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(friendService.getMyFriends(userId));
    }

    /** 친구를 삭제한다. */
    @Operation(summary = "친구 삭제")
    @DeleteMapping("/{friendUserId}")
    public ApiResponse<Void> removeFriend(
            @AuthenticationPrincipal Long userId, @PathVariable Long friendUserId) {
        friendService.removeFriend(userId, friendUserId);
        return ApiResponse.ok();
    }
}
