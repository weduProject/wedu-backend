package com.wedu.auth.service;

import com.wedu.auth.dto.EmailAuthResponse;
import com.wedu.auth.dto.EmailLoginRequest;
import com.wedu.auth.dto.EmailSignupRequest;
import com.wedu.auth.support.EmailNormalizer;
import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.global.security.jwt.JwtTokenProvider;
import com.wedu.user.domain.Nickname;
import com.wedu.user.domain.SocialProvider;
import com.wedu.user.domain.User;
import com.wedu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 이메일 기반 자체 회원가입/로그인 유스케이스. */
@Service
@RequiredArgsConstructor
public class EmailAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public EmailAuthResponse signup(EmailSignupRequest request) {
        if (!request.password().equals(request.passwordConfirm())) {
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_CONFIRM_MISMATCH);
        }

        String email = EmailNormalizer.normalize(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS);
        }

        String passwordHash = passwordEncoder.encode(request.password());
        User user = saveLocalUser(email, request.name(), passwordHash);
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        return EmailAuthResponse.from(accessToken, user);
    }

    @Transactional(readOnly = true)
    public EmailAuthResponse login(EmailLoginRequest request) {
        String email = EmailNormalizer.normalize(request.email());
        User user = userRepository
                .findByProviderAndSocialId(SocialProvider.LOCAL, email)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        return EmailAuthResponse.from(accessToken, user);
    }

    private User saveLocalUser(String email, String name, String passwordHash) {
        try {
            return userRepository.saveAndFlush(
                    User.registerLocal(email, new Nickname(name), passwordHash));
        } catch (DataIntegrityViolationException ex) {
            if (isEmailUniqueViolation(ex)) {
                throw new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS);
            }
            throw ex;
        }
    }

    private boolean isEmailUniqueViolation(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause().getMessage();
        return message != null && message.toLowerCase().contains("uk_users_email");
    }
}
