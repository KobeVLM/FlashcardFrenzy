package com.marikit.flashcardfrenzy.auth;

import com.marikit.flashcardfrenzy.common.response.ApiResponse;
import com.marikit.flashcardfrenzy.common.security.LoginRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * MVP — View (Controller)
 * HTTP routing and response wrapping only. All logic in AuthPresenter.
 *
 * Routes:
 *   POST /api/v1/auth/register   — public
 *   POST /api/v1/auth/login      — public  (rate-limited: 10 req/min per IP)
 *   POST /api/v1/auth/logout     — requires auth
 *   POST /api/v1/auth/refresh    — public  (requires valid refresh token in body)
 *   GET  /api/v1/auth/me         — requires auth
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthPresenter authPresenter;
    private final LoginRateLimiter loginRateLimiter;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthDto.AuthResponse>> register(
            @Valid @RequestBody AuthDto.RegisterRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(authPresenter.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDto.AuthResponse>> login(
            @Valid @RequestBody AuthDto.LoginRequest request,
            HttpServletRequest httpRequest) {

        // Rate limit by client IP — throws RateLimitException if exceeded
        String clientIp = getClientIp(httpRequest);
        loginRateLimiter.checkLimit(clientIp);

        return ResponseEntity.ok(ApiResponse.ok(authPresenter.login(request)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        // JWT is stateless — the client must discard both tokens.
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthDto.RefreshResponse>> refresh(
            @Valid @RequestBody AuthDto.RefreshRequest request) {

        return ResponseEntity.ok(ApiResponse.ok(authPresenter.refresh(request)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthDto.UserResponse>> getProfile(Principal principal) {
        return ResponseEntity.ok(ApiResponse.ok(authPresenter.getProfile(principal.getName())));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    /**
     * Extracts the real client IP, accounting for reverse proxy headers
     * (e.g., when deployed behind Render's load balancer).
     */
    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
