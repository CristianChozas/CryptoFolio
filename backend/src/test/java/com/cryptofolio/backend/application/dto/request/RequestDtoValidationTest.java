package com.cryptofolio.backend.application.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RequestDtoValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void givenValidRegisterUserRequest_whenValidating_thenNoViolations() {
        RegisterUserRequest request = new RegisterUserRequest("cristian", "cristian@example.com", "password123");

        Set<ConstraintViolation<RegisterUserRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "plainaddress"})
    void givenRegisterUserRequestWithInvalidEmail_whenValidating_thenRejectsEmail(String email) {
        RegisterUserRequest request = new RegisterUserRequest("cristian", email, "password123");

        Set<ConstraintViolation<RegisterUserRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("email");
    }

    @Test
    void givenLoginRequestWithBlankPassword_whenValidating_thenRejectsPassword() {
        LoginRequest request = new LoginRequest("cristian@example.com", " ");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("password");
    }

    @Test
    void givenPortfolioRequestWithBlankName_whenValidating_thenRejectsName() {
        CreatePortfolioRequest request = new CreatePortfolioRequest("", "Long-term holdings");

        Set<ConstraintViolation<CreatePortfolioRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("name");
    }

    @Test
    void givenTransactionRequestWithLowercaseCrypto_whenValidating_thenRejectsCryptoFormat() {
        AddTransactionRequest request = new AddTransactionRequest(
                1L,
                "btc",
                "BUY",
                new BigDecimal("0.10"),
                new BigDecimal("65000.00"));

        Set<ConstraintViolation<AddTransactionRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("crypto");
    }

    @Test
    void givenTransactionRequestWithNonPositiveNumbers_whenValidating_thenRejectsNumericFields() {
        AddTransactionRequest request = new AddTransactionRequest(
                0L,
                "BTC",
                "BUY",
                BigDecimal.ZERO,
                new BigDecimal("-1.00"));

        Set<ConstraintViolation<AddTransactionRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder("portfolioId", "amount", "pricePerUnit");
    }
}
