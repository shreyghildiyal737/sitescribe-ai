package com.sitescribe.auth;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class AuthRateLimitFilterTest {

    /** A clock the test can move forward, so the window rollover is checked without sleeping. */
    private static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-11-04T12:00:00Z");
        void advance(Duration by) { now = now.plus(by); }
        @Override public Instant instant() { return now; }
        @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
    }

    private static int call(AuthRateLimitFilter filter, String uri, String ip) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", uri);
        request.setRemoteAddr(ip);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response.getStatus();
    }

    @Test
    void blocksAttemptsBeyondTheLimitWithinAWindow() throws Exception {
        AuthRateLimitFilter filter = new AuthRateLimitFilter(3, new MutableClock());

        for (int i = 0; i < 3; i++) {
            assertThat(call(filter, "/api/auth/login", "10.0.0.1")).isEqualTo(200);
        }
        assertThat(call(filter, "/api/auth/login", "10.0.0.1")).isEqualTo(429);
    }

    @Test
    void countsEachClientSeparately() throws Exception {
        AuthRateLimitFilter filter = new AuthRateLimitFilter(1, new MutableClock());

        assertThat(call(filter, "/api/auth/login", "10.0.0.1")).isEqualTo(200);
        assertThat(call(filter, "/api/auth/login", "10.0.0.2")).isEqualTo(200);
        assertThat(call(filter, "/api/auth/login", "10.0.0.1")).isEqualTo(429);
    }

    @Test
    void allowsAttemptsAgainOnceTheWindowHasPassed() throws Exception {
        MutableClock clock = new MutableClock();
        AuthRateLimitFilter filter = new AuthRateLimitFilter(1, clock);

        assertThat(call(filter, "/api/auth/login", "10.0.0.1")).isEqualTo(200);
        assertThat(call(filter, "/api/auth/login", "10.0.0.1")).isEqualTo(429);

        clock.advance(Duration.ofSeconds(60));
        assertThat(call(filter, "/api/auth/login", "10.0.0.1")).isEqualTo(200);
    }

    @Test
    void leavesTheRestOfTheApiAlone() throws Exception {
        AuthRateLimitFilter filter = new AuthRateLimitFilter(1, new MutableClock());

        for (int i = 0; i < 5; i++) {
            assertThat(call(filter, "/api/projects/my", "10.0.0.1")).isEqualTo(200);
        }
    }
}
