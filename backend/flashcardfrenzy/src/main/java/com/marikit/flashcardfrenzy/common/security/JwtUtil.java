package com.marikit.flashcardfrenzy.common.security;

import com.marikit.flashcardfrenzy.common.exception.TokenExpiredException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Handles JWT generation, parsing, and validation for both
 * access tokens and refresh tokens.
 *
 * Access token  — short-lived (24h), type = "access"
 * Refresh token — long-lived  (7d),  type = "refresh"
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long accessExpirationMs;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // ── Token generation ──────────────────────────────────────────────────────

    public String generateAccessToken(String email, String role) {
        return buildToken(email, role, "access", accessExpirationMs);
    }

    public String generateRefreshToken(String email, String role) {
        return buildToken(email, role, "refresh", refreshExpirationMs);
    }

    private String buildToken(String email, String role, String type, long expiryMs) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .claim("type", type)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ── Token parsing ─────────────────────────────────────────────────────────

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public String extractType(String token) {
        return extractClaims(token).get("type", String.class);
    }

    // ── Validation ────────────────────────────────────────────────────────────

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("JWT token has expired");
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Validates that the token is a refresh token (not an access token).
     * Prevents using an access token to call POST /auth/refresh.
     */
    public boolean isRefreshToken(String token) {
        return "refresh".equals(extractType(token));
    }
}
