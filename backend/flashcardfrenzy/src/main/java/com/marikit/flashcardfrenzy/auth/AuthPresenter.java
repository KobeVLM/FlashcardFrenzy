package com.marikit.flashcardfrenzy.auth;

import com.marikit.flashcardfrenzy.common.exception.DuplicateResourceException;
import com.marikit.flashcardfrenzy.common.exception.ResourceNotFoundException;
import com.marikit.flashcardfrenzy.common.exception.TokenExpiredException;
import com.marikit.flashcardfrenzy.common.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * MVP — Presenter
 * Handles all auth business logic: register, login, token refresh, profile.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthPresenter {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // ── Register ──────────────────────────────────────────────────────────────

    public AuthDto.AuthResponse register(AuthDto.RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email is already in use");
        }

        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(User.Role.USER)
                .build();

        userRepository.save(user);
        return buildAuthResponse(user);
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        return buildAuthResponse(user);
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    /**
     * Validates the refresh token and issues a new access token.
     * Only refresh tokens are accepted — passing an access token returns 401.
     */
    public AuthDto.RefreshResponse refresh(AuthDto.RefreshRequest request) {
        String token = request.refreshToken();

        try {
            if (!jwtUtil.isTokenValid(token)) {
                throw new BadCredentialsException("Invalid refresh token");
            }
        } catch (TokenExpiredException e) {
            throw new TokenExpiredException("Refresh token has expired. Please log in again.");
        }

        if (!jwtUtil.isRefreshToken(token)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        String email = jwtUtil.extractEmail(token);
        String role  = jwtUtil.extractRole(token);

        // Verify user still exists
        userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        return new AuthDto.RefreshResponse(jwtUtil.generateAccessToken(email, role));
    }

    // ── Profile ───────────────────────────────────────────────────────────────

    public AuthDto.UserResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return new AuthDto.UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                user.getCreatedAt() != null ? user.getCreatedAt().toString() : java.time.Instant.now().toString()
        );
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private AuthDto.AuthResponse buildAuthResponse(User user) {
        String role = user.getRole().name();
        return new AuthDto.AuthResponse(
                jwtUtil.generateAccessToken(user.getEmail(), role),
                jwtUtil.generateRefreshToken(user.getEmail(), role),
                user.getEmail(),
                user.getFullName(),
                role
        );
    }
}
