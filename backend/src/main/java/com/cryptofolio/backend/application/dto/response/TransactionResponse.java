package com.cryptofolio.backend.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public class TransactionResponse {

    private final Long id;
    private final String crypto;
    private final String type;
    private final BigDecimal amount;
    private final BigDecimal pricePerUnit;
    private final Instant timestamp;

    public TransactionResponse(Long id, String crypto, String type, BigDecimal amount, BigDecimal pricePerUnit,
            Instant timestamp) {
        this.id = id;
        this.crypto = crypto;
        this.type = type;
        this.amount = amount;
        this.pricePerUnit = pricePerUnit;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public String getCrypto() {
        return crypto;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getPricePerUnit() {
        return pricePerUnit;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
