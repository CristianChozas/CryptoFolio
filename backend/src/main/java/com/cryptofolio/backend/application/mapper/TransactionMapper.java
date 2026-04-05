package com.cryptofolio.backend.application.mapper;

import com.cryptofolio.backend.application.dto.request.AddTransactionRequest;
import com.cryptofolio.backend.application.dto.response.TransactionResponse;
import com.cryptofolio.backend.domain.model.Transaction;
import com.cryptofolio.backend.domain.valueobject.TransactionType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.Instant;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "portfolioId", source = "request.portfolioId")
    @Mapping(target = "crypto", source = "request.crypto")
    @Mapping(target = "type", source = "request.type")
    @Mapping(target = "amount", source = "request.amount")
    @Mapping(target = "pricePerUnit", source = "request.pricePerUnit")
    @Mapping(target = "timestamp", source = "timestamp")
    Transaction toTransaction(AddTransactionRequest request, Instant timestamp);

    TransactionResponse toTransactionResponse(Transaction transaction);

    default TransactionType map(String type) {
        return TransactionType.from(type);
    }
}
