package com.cryptofolio.backend.application.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
    void shouldAcceptValidRegisterUserRequest() {
        RegisterUserRequest request = new RegisterUserRequest("cristian", "cristian@example.com", "password123");

        Set<ConstraintViolation<RegisterUserRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldRejectBlankLoginPassword() {
        LoginRequest request = new LoginRequest("cristian@example.com", " ");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("password");
    }

    @Test
    void shouldRejectBlankPortfolioName() {
        CreatePortfolioRequest request = new CreatePortfolioRequest("", "Long-term holdings");

        Set<ConstraintViolation<CreatePortfolioRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("name");
    }

    @Test
    void shouldRejectInvalidTransactionFields() {
        AddTransactionRequest request = new AddTransactionRequest(
                0L,
                "btc",
                "hold",
                BigDecimal.ZERO,
                new BigDecimal("-1.00"));

        Set<ConstraintViolation<AddTransactionRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder("portfolioId", "crypto", "type", "amount", "pricePerUnit");
    }
}
