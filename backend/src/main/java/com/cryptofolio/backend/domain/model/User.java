package com.cryptofolio.backend.domain.model;

import java.time.Instant;
import java.util.regex.Pattern;

public class User {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final Long id;
    private final String username;
    private final String email;
    private final String passwordHash;
    private final Instant createdAt;

    public User(Long id, String username, String email, String passwordHash, Instant createdAt) {
        this.id = id;
        this.username = requireNonNull(normalizeField(username), "username");
        this.email = validateEmail(email);
        this.passwordHash = requireNonNull(normalizeField(passwordHash), "passwordHash");
        this.createdAt = requireNonNull(createdAt, "createdAt");
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public User updateEmail(String newEmail) {
        String validatedEmail = validateEmail(newEmail);

        if (validatedEmail.equals(this.email)) {
            throw new IllegalArgumentException("newEmail cannot be the same as the current email");
        }

        return new User(id, username, validatedEmail, passwordHash, createdAt);
    }

    public boolean isEmailValid(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    private String validateEmail(String email) {
        String normalizedEmail = requireNonNull(normalizeField(email), "email");

        if (!isEmailValid(normalizedEmail)) {
            throw new IllegalArgumentException("email must be a valid email address");
        }

        return normalizedEmail;
    }

    @SuppressWarnings("unchecked")
    private <T> T normalizeField(T value) {
        if (value instanceof String stringValue) {
            String trimmedValue = stringValue.trim();
            return (T) (trimmedValue.isEmpty() ? null : trimmedValue);
        }

        return value;
    }

    private <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }

        return value;
    }
}
