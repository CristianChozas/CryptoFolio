package com.cryptofolio.backend.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class UserTest {

    private static final Long ID = 1L;
    private static final String USERNAME = "john";
    private static final String EMAIL = "john@example.com";
    private static final String PASSWORD_HASH = "bcrypt_hash";
    private static final Instant CREATED_AT = Instant.parse("2026-01-15T10:30:00Z");

    @Test
    void shouldCreateUserWithNormalizedValues() {
        User user = new User(ID, "  john  ", "  john@example.com  ", "  bcrypt_hash  ", CREATED_AT);

        assertEquals(ID, user.getId());
        assertEquals("john", user.getUsername());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("bcrypt_hash", user.getPasswordHash());
        assertEquals(CREATED_AT, user.getCreatedAt());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void shouldRejectBlankUsername(String username) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new User(ID, username, EMAIL, PASSWORD_HASH, CREATED_AT));

        assertEquals("username cannot be null", ex.getMessage());
    }

    @Test
    void shouldRejectNullUsername() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new User(ID, null, EMAIL, PASSWORD_HASH, CREATED_AT));

        assertEquals("username cannot be null", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void shouldRejectBlankEmail(String email) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new User(ID, USERNAME, email, PASSWORD_HASH, CREATED_AT));

        assertEquals("email cannot be null", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "missing@domain", "@nodomain.com", "user@.com"})
    void shouldRejectInvalidEmailFormat(String email) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new User(ID, USERNAME, email, PASSWORD_HASH, CREATED_AT));

        assertEquals("email must be a valid email address", ex.getMessage());
    }

    @Test
    void shouldRejectNullCreatedAt() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new User(ID, USERNAME, EMAIL, PASSWORD_HASH, null));

        assertEquals("createdAt cannot be null", ex.getMessage());
    }

    @Test
    void shouldUpdateEmailAndKeepImmutability() {
        User user = new User(ID, USERNAME, EMAIL, PASSWORD_HASH, CREATED_AT);

        User updated = user.updateEmail("new@example.com");

        assertEquals("new@example.com", updated.getEmail());
        assertEquals(EMAIL, user.getEmail());
        assertNotSame(user, updated);
        assertEquals(user.getId(), updated.getId());
        assertEquals(user.getUsername(), updated.getUsername());
    }

    @Test
    void shouldRejectUpdatingWithSameEmail() {
        User user = new User(ID, USERNAME, EMAIL, PASSWORD_HASH, CREATED_AT);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> user.updateEmail("  john@example.com  "));

        assertEquals("newEmail cannot be the same as the current email", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"john@example.com", "name+tag@domain.org", "a@b.c"})
    void shouldReturnTrueForValidEmail(String email) {
        User user = new User(ID, USERNAME, EMAIL, PASSWORD_HASH, CREATED_AT);
        assertTrue(user.isEmailValid(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "no-at.com", "user @domain.com", ""})
    void shouldReturnFalseForInvalidEmail(String email) {
        User user = new User(ID, USERNAME, EMAIL, PASSWORD_HASH, CREATED_AT);
        assertFalse(user.isEmailValid(email));
    }
}
