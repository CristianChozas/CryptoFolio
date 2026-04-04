package com.cryptofolio.backend.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PortfolioTest {

    private static final Long ID = 1L;
    private static final String NAME = "Main Portfolio";
    private static final String DESCRIPTION = "Long-term holdings";
    private static final Long USER_ID = 10L;
    private static final Instant CREATED_AT = Instant.parse("2026-01-15T10:30:00Z");

    @Test
    void shouldCreatePortfolioWithNormalizedValues() {
        Portfolio portfolio = new Portfolio(ID, "  Main Portfolio  ", "  Long-term holdings  ", USER_ID, CREATED_AT);

        assertEquals(ID, portfolio.getId());
        assertEquals("Main Portfolio", portfolio.getName());
        assertEquals("Long-term holdings", portfolio.getDescription());
        assertEquals(USER_ID, portfolio.getUserId());
        assertEquals(CREATED_AT, portfolio.getCreatedAt());
    }

    @Test
    void shouldNormalizeBlankDescriptionToNull() {
        Portfolio portfolio = new Portfolio(ID, NAME, "   ", USER_ID, CREATED_AT);
        assertNull(portfolio.getDescription());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void shouldRejectBlankName(String name) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Portfolio(ID, name, DESCRIPTION, USER_ID, CREATED_AT));

        assertEquals("name cannot be null", ex.getMessage());
    }

    @Test
    void shouldRejectNullUserId() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Portfolio(ID, NAME, DESCRIPTION, null, CREATED_AT));

        assertEquals("userId cannot be null", ex.getMessage());
    }

    @Test
    void shouldRejectNullCreatedAt() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Portfolio(ID, NAME, DESCRIPTION, USER_ID, null));

        assertEquals("createdAt cannot be null", ex.getMessage());
    }

    @Test
    void shouldRenameAndKeepImmutability() {
        Portfolio portfolio = new Portfolio(ID, NAME, DESCRIPTION, USER_ID, CREATED_AT);

        Portfolio renamed = portfolio.rename("Updated Portfolio");

        assertEquals("Updated Portfolio", renamed.getName());
        assertEquals(NAME, portfolio.getName());
        assertNotSame(portfolio, renamed);
        assertEquals(portfolio.getUserId(), renamed.getUserId());
    }

    @Test
    void shouldRejectRenamingWithSameName() {
        Portfolio portfolio = new Portfolio(ID, NAME, DESCRIPTION, USER_ID, CREATED_AT);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> portfolio.rename("  Main Portfolio  "));

        assertEquals("newName cannot be the same as the current name", ex.getMessage());
    }

    @Test
    void shouldUpdateDescriptionAndKeepImmutability() {
        Portfolio portfolio = new Portfolio(ID, NAME, DESCRIPTION, USER_ID, CREATED_AT);

        Portfolio updated = portfolio.updateDescription("Updated description");

        assertEquals("Updated description", updated.getDescription());
        assertEquals(DESCRIPTION, portfolio.getDescription());
        assertNotSame(portfolio, updated);
    }

    @Test
    void shouldReturnSameInstanceWhenDescriptionDoesNotChange() {
        Portfolio portfolio = new Portfolio(ID, NAME, DESCRIPTION, USER_ID, CREATED_AT);

        Portfolio same = portfolio.updateDescription("  Long-term holdings  ");

        assertSame(portfolio, same);
    }

    @Test
    void shouldAllowDescriptionToBecomeNull() {
        Portfolio portfolio = new Portfolio(ID, NAME, DESCRIPTION, USER_ID, CREATED_AT);

        Portfolio updated = portfolio.updateDescription("   ");

        assertNull(updated.getDescription());
    }
}
