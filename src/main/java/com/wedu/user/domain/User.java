package com.wedu.user.domain;

import com.wedu.global.common.BaseTimeEntity;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 Aggregate Root (user Bounded Context 의 진입점).
 *
 * <p>회원(001)·온보딩(002)·마이페이지(018)의 상태와 규칙을 이 애그리게이트가 소유한다.
 * 상태 변경은 반드시 도메인 메서드({@link #register}, {@link #completeOnboarding},
 * {@link #updateProfile})를 통해서만 일어나며, setter 를 두지 않아 불변식이 밖에서 깨지지 않게 한다
 * (빈약한 도메인 모델 회피 — Fowler, Anemic Domain Model 안티패턴).
 *
 * <p>다른 애그리게이트는 이 애그리게이트를 객체 참조가 아니라 {@code id} 로 참조한다
 * (Vernon, IDDD — "작은 애그리게이트, 식별자로 참조").
 */
@Getter
@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_users_provider_social_id",
                columnNames = {"provider", "social_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SocialProvider provider;

    /** 소셜 제공자 내부의 사용자 고유 식별자. {@code provider} 와 함께 유일. */
    @Column(name = "social_id", nullable = false, length = 100)
    private String socialId;

    @Column(nullable = false, length = 255)
    private String email;

    @Embedded
    private Nickname nickname;

    @Column(name = "profile_image_url", length = 512)
    private String profileImageUrl;

    /** 온보딩(초기 설정) 완료 여부. 최초 로그인 사용자는 false 로 시작한다. */
    @Column(name = "onboarding_completed", nullable = false)
    private boolean onboardingCompleted;

    private User(
            SocialProvider provider,
            String socialId,
            String email,
            Nickname nickname,
            String profileImageUrl) {
        this.provider = provider;
        this.socialId = socialId;
        this.email = email;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.onboardingCompleted = false;
    }

    /**
     * 소셜 로그인 최초 가입 팩토리. 필수 불변식(제공자·소셜ID·이메일·닉네임)을 여기서 강제한다.
     */
    public static User register(
            SocialProvider provider,
            String socialId,
            String email,
            Nickname nickname,
            String profileImageUrl) {
        if (provider == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "소셜 제공자는 필수입니다.");
        }
        if (socialId == null || socialId.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "소셜 식별자는 필수입니다.");
        }
        if (email == null || email.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이메일은 필수입니다.");
        }
        if (nickname == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "닉네임은 필수입니다.");
        }
        return new User(provider, socialId, email, nickname, profileImageUrl);
    }

    /**
     * 온보딩 완료 처리. 이미 완료한 사용자를 다시 온보딩하려 하면 규칙 위반이다.
     */
    public void completeOnboarding() {
        if (this.onboardingCompleted) {
            throw new BusinessException(ErrorCode.USER_ALREADY_ONBOARDED);
        }
        this.onboardingCompleted = true;
    }

    /** 마이페이지 프로필 수정. 프로필 이미지는 없을 수 있다(null 허용). */
    public void updateProfile(Nickname nickname, String profileImageUrl) {
        if (nickname == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "닉네임은 필수입니다.");
        }
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }
}
