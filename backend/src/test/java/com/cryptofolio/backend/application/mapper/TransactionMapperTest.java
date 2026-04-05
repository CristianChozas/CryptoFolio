package com.cryptofolio.backend.application.mapper;

import com.cryptofolio.backend.application.dto.request.AddTransactionRequest;
import com.cryptofolio.backend.application.dto.response.TransactionResponse;
import com.cryptofolio.backend.domain.model.Transaction;
import com.cryptofolio.backend.domain.valueobject.TransactionType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionMapperTest {

    private final TransactionMapper mapper = Mappers.getMapper(TransactionMapper.class);

    @Test
    void shouldMapAddTransactionRequestToTransaction() {
        AddTransactionRequest request = new AddTransactionRequest(
                42L,
                "BTC",
                "BUY",
                new BigDecimal("0.15000000"),
                new BigDecimal("65000.00"));
        Instant timestamp = Instant.parse("2026-04-05T12:00:00Z");

        Transaction transaction = mapper.toTransaction(request, timestamp);

        assertThat(transaction.getId()).isNull();
        assertThat(transaction.getPortfolioId()).isEqualTo(42L);
        assertThat(transaction.getCrypto()).isEqualTo("BTC");
        assertThat(transaction.getType()).isEqualTo(TransactionType.BUY);
        assertThat(transaction.getAmount()).isEqualByComparingTo("0.15000000");
        assertThat(transaction.getPricePerUnit()).isEqualByComparingTo("65000.00");
        assertThat(transaction.getTimestamp()).isEqualTo(timestamp);
    }

    @Test
    void shouldMapTransactionToResponse() {
        Transaction transaction = new Transaction(
                3L,
                42L,
                "BTC",
                TransactionType.SELL,
                new BigDecimal("0.10000000"),
                new BigDecimal("70000.00"),
                Instant.parse("2026-04-05T13:00:00Z"));

        TransactionResponse response = mapper.toTransactionResponse(transaction);

        assertThat(response.getId()).isEqualTo(3L);
        assertThat(response.getCrypto()).isEqualTo("BTC");
        assertThat(response.getType()).isEqualTo("SELL");
        assertThat(response.getAmount()).isEqualByComparingTo("0.10000000");
        assertThat(response.getPricePerUnit()).isEqualByComparingTo("70000.00");
    }
}
