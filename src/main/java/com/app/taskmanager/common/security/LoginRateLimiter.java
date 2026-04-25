package com.app.taskmanager.common.security;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.app.taskmanager.exception.TooManyRequestsException;

import lombok.extern.slf4j.Slf4j;

/**
 * In-memory fixed-window rate limiter for the login endpoint.
 *
 * Each key (e.g. client IP, target email) gets its own counter that resets
 * once the configured window elapses. Counters are evicted in a periodic
 * cleanup pass so memory stays bounded under sustained attack.
 *
 * <p>Suitable for a single-instance deployment. For horizontally scaled
 * deployments, swap the in-memory map for a shared backend (Redis +
 * Bucket4j) so all replicas honor the same budget.
 */
@Slf4j
@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS_PER_WINDOW = 5;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final ConcurrentMap<String, Counter> counters = new ConcurrentHashMap<>();

    /**
     * Records an attempt for the given key. Throws {@link TooManyRequestsException}
     * once the per-window budget is exhausted.
     */
    public void check(String key) {
        long now = System.currentTimeMillis();
        Counter counter = counters.compute(key, (k, existing) -> {
            if (existing == null || now - existing.windowStart >= WINDOW.toMillis()) {
                return new Counter(now);
            }
            existing.attempts.incrementAndGet();
            return existing;
        });

        int current = counter.attempts.get();
        if (current > MAX_ATTEMPTS_PER_WINDOW) {
            log.warn("Rate limit exceeded for key '{}' ({} attempts in window)", key, current);
            throw new TooManyRequestsException(
                    "Too many login attempts. Try again in %d seconds."
                            .formatted(WINDOW.toSeconds()));
        }
    }

    /**
     * Periodically drops counters whose window expired more than one window ago,
     * so that long-tail abuse cannot grow the map without bound.
     */
    @Scheduled(fixedDelay = 5 * 60 * 1000L, initialDelay = 5 * 60 * 1000L)
    public void evictStale() {
        long cutoff = System.currentTimeMillis() - 2 * WINDOW.toMillis();
        int before = counters.size();
        counters.entrySet().removeIf(e -> e.getValue().windowStart < cutoff);
        int removed = before - counters.size();
        if (removed > 0) {
            log.debug("Evicted {} stale rate-limit counters (size now {})", removed, counters.size());
        }
    }

    private static final class Counter {
        final long windowStart;
        final AtomicInteger attempts;

        Counter(long windowStart) {
            this.windowStart = windowStart;
            this.attempts = new AtomicInteger(1);
        }
    }
}
