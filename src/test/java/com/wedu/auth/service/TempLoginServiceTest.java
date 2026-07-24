package com.wedu.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wedu.auth.dto.TempLoginRequest;
import com.wedu.auth.dto.TempLoginResponse;
import com.wedu.global.security.jwt.JwtTokenProvider;
import com.wedu.user.domain.Nickname;
import com.wedu.user.domain.SocialProvider;
import com.wedu.user.domain.User;
import com.wedu.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TempLoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private TempLoginService tempLoginService;

    @Test
    @DisplayName("임시 유저가 없으면 생성하고 JWT를 발급한다")
    void createTempUserAndIssueToken() {
        TempLoginRequest request = new TempLoginRequest("temp@example.com", "테스터");
        User savedUser = tempUser(1L, "temp@example.com", "테스터");

        when(userRepository.findByProviderAndSocialId(SocialProvider.KAKAO, "temp:temp@example.com"))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtTokenProvider.createAccessToken(1L)).thenReturn("access-token");

        TempLoginResponse response = tempLoginService.login(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("temp@example.com");
        assertThat(response.nickname()).isEqualTo("테스터");
        assertThat(response.tokenType()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("이미 존재하는 임시 유저는 재사용하고 새 JWT만 발급한다")
    void reuseTempUserAndIssueToken() {
        TempLoginRequest request = new TempLoginRequest("temp@example.com", "새닉네임");
        User existingUser = tempUser(2L, "temp@example.com", "기존닉네임");

        when(userRepository.findByProviderAndSocialId(SocialProvider.KAKAO, "temp:temp@example.com"))
                .thenReturn(Optional.of(existingUser));
        when(jwtTokenProvider.createAccessToken(2L)).thenReturn("new-access-token");

        TempLoginResponse response = tempLoginService.login(request);

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.userId()).isEqualTo(2L);
        assertThat(response.nickname()).isEqualTo("기존닉네임");
        verify(userRepository, never()).save(any(User.class));
    }

    private User tempUser(Long id, String email, String nickname) {
        User user = User.register(
                SocialProvider.KAKAO, "temp:" + email, email, new Nickname(nickname), null);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
