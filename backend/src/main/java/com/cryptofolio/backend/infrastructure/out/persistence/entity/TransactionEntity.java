package com.cryptofolio.backend.infrastructure.out.persistence.entity;

import com.cryptofolio.backend.domain.valueobject.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String crypto;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 4)
    private TransactionType type;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal amount;

    @Column(name = "price_per_unit", nullable = false, precision = 20, scale = 2)
    private BigDecimal pricePerUnit;

    @Column(nullable = false)
    private Instant timestamp;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private PortfolioEntity portfolio;

    protected TransactionEntity() {
    }

    public TransactionEntity(Long id, String crypto, TransactionType type, BigDecimal amount, BigDecimal pricePerUnit,
            Instant timestamp, PortfolioEntity portfolio) {
        this.id = id;
        this.crypto = crypto;
        this.type = type;
        this.amount = amount;
        this.pricePerUnit = pricePerUnit;
        this.timestamp = timestamp;
        this.portfolio = portfolio;
    }

    public Long getId() {
        return id;
    }

    public String getCrypto() {
        return crypto;
    }

    public TransactionType getType() {
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

    public PortfolioEntity getPortfolio() {
        return portfolio;
    }
}
