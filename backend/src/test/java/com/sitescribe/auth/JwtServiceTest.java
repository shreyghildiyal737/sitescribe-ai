package com.sitescribe.auth;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    @Test
    void refusesASecretShorterThan32Bytes() {
        assertThatThrownBy(() -> JwtService.keyFrom("too-short"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("at least 32 bytes");
    }

    @Test
    void withNoSecretEachProcessGetsItsOwnRandomKey() {
        // The old code fell back to a default committed in the repo, so every unconfigured
        // deployment shared one forgeable key.
        assertThat(JwtService.keyFrom("").getEncoded())
            .isNotEqualTo(JwtService.keyFrom("").getEncoded());
    }

    @Test
    void acceptsABase64SecretOfFullLength() {
        String secret = "c2l0ZXNjcmliZS10ZXN0LXNlY3JldC10aGF0LWlzLWxvbmctZW5vdWdo";
        assertThat(JwtService.keyFrom(secret).getEncoded()).hasSizeGreaterThanOrEqualTo(32);
    }
}
