package com.cryptofolio.backend.infrastructure.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef";

    @Test
    void givenUserId_whenGeneratingToken_thenCanReadAndValidateIt() {
        JwtTokenProvider provider = new JwtTokenProvider(
                SECRET,
                86_400_000L,
                Clock.fixed(Instant.parse("2026-04-06T16:00:00Z"), ZoneOffset.UTC));

        String token = provider.generateToken(42L);

        assertThat(provider.validateToken(token)).isTrue();
        assertThat(provider.getUserIdFromToken(token)).isEqualTo(42L);
    }

    @Test
    void givenExpiredToken_whenValidating_thenReturnsFalse() {
        JwtTokenProvider generatingProvider = new JwtTokenProvider(
                SECRET,
                1_000L,
                Clock.fixed(Instant.parse("2026-04-06T16:00:00Z"), ZoneOffset.UTC));
        String token = generatingProvider.generateToken(7L);

        JwtTokenProvider validatingProvider = new JwtTokenProvider(
                SECRET,
                1_000L,
                Clock.fixed(Instant.parse("2026-04-06T16:00:02Z"), ZoneOffset.UTC));

        assertThat(validatingProvider.validateToken(token)).isFalse();
    }

    @Test
    void givenTokenSignedWithDifferentSecret_whenValidating_thenReturnsFalse() {
        JwtTokenProvider generatingProvider = new JwtTokenProvider(
                SECRET,
                86_400_000L,
                Clock.fixed(Instant.parse("2026-04-06T16:00:00Z"), ZoneOffset.UTC));
        String token = generatingProvider.generateToken(99L);

        JwtTokenProvider validatingProvider = new JwtTokenProvider(
                "fedcba9876543210fedcba9876543210",
                86_400_000L,
                Clock.fixed(Instant.parse("2026-04-06T16:00:01Z"), ZoneOffset.UTC));

        assertThat(validatingProvider.validateToken(token)).isFalse();
    }
}
