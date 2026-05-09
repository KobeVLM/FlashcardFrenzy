package com.marikit.flashcardfrenzy.common.security;

import com.marikit.flashcardfrenzy.common.exception.RateLimitException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bucket4j-based in-memory rate limiter scoped to login attempts per IP.
 * SDD requirement: max 10 attempts per minute per IP address.
 *
 * Each IP gets its own token bucket. Buckets are created lazily on first request.
 */
@Component
public class LoginRateLimiter {

    @Value("${rate-limit.login.capacity:10}")
    private int capacity;

    @Value("${rate-limit.login.refill-minutes:1}")
    private int refillMinutes;

    // IP address → Bucket
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Checks if the given IP is allowed to attempt a login.
     * Throws RateLimitException if the limit has been exceeded.
     */
    public void checkLimit(String ipAddress) {
        Bucket bucket = buckets.computeIfAbsent(ipAddress, this::newBucket);
        if (!bucket.tryConsume(1)) {
            throw new RateLimitException(
                "Too many login attempts. Please try again in " + refillMinutes + " minute(s)."
            );
        }
    }

    private Bucket newBucket(String ip) {
        Bandwidth limit = Bandwidth.classic(
            capacity,
            Refill.greedy(capacity, Duration.ofMinutes(refillMinutes))
        );
        return Bucket.builder().addLimit(limit).build();
    }
}
