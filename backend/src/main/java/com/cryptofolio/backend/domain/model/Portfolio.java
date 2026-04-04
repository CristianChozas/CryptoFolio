package com.cryptofolio.backend.domain.model;

import java.time.Instant;

public class Portfolio {

    private final Long id;
    private final String name;
    private final String description;
    private final Long userId;
    private final Instant createdAt;

    public Portfolio(Long id, String name, String description, Long userId, Instant createdAt) {
        this.id = id;
        this.name = requireNonNull(normalizeField(name), "name");
        this.description = normalizeField(description);
        this.userId = requireNonNull(userId, "userId");
        this.createdAt = requireNonNull(createdAt, "createdAt");
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Long getUserId() {
        return userId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Portfolio rename(String newName) {
        String validatedName = requireNonNull(normalizeField(newName), "newName");

        if (validatedName.equals(this.name)) {
            throw new IllegalArgumentException("newName cannot be the same as the current name");
        }

        return new Portfolio(id, validatedName, description, userId, createdAt);
    }

    public Portfolio updateDescription(String newDescription) {
        String normalizedDescription = normalizeField(newDescription);

        if ((normalizedDescription == null && this.description == null)
                || (normalizedDescription != null && normalizedDescription.equals(this.description))) {
            return this;
        }

        return new Portfolio(id, name, normalizedDescription, userId, createdAt);
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
