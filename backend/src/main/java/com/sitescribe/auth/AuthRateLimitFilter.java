package com.sitescribe.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caps login and sign-up attempts per client address in fixed one-minute windows, to slow
 * password guessing and sign-up spam on the only endpoints open to anonymous callers.
 *
 * In-memory, so the limit is per instance and resets on restart; enough for a single-node
 * deployment, not for a fleet. The address is {@code getRemoteAddr()}, which behind a proxy is
 * only the real client if forwarded headers are trusted (server.forward-headers-strategy).
 */
@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_MS = 60_000;
    private static final int MAX_TRACKED_CLIENTS = 10_000;

    private final int maxPerWindow;
    private final Clock clock;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    @Autowired
    public AuthRateLimitFilter(@Value("${sitescribe.auth-rate-limit-per-minute:10}") int maxPerWindow) {
        this(maxPerWindow, Clock.systemUTC());
    }

    AuthRateLimitFilter(int maxPerWindow, Clock clock) {
        this.maxPerWindow = maxPerWindow;
        this.clock = clock;
    }

    private record Window(long startedAt, int count) {}

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        long now = clock.millis();
        if (windows.size() > MAX_TRACKED_CLIENTS) {
            // Bound memory under a spray of distinct addresses by dropping finished windows.
            windows.values().removeIf(w -> now - w.startedAt() >= WINDOW_MS);
        }

        Window window = windows.compute(request.getRemoteAddr(), (addr, current) ->
            current == null || now - current.startedAt() >= WINDOW_MS
                ? new Window(now, 1)
                : new Window(current.startedAt(), current.count() + 1));

        if (window.count() > maxPerWindow) {
            long retryAfterSeconds = Math.max(1, (window.startedAt() + WINDOW_MS - now + 999) / 1000);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                "{\"success\":false,\"data\":null,\"message\":\"Too many attempts. Try again in a minute.\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
