package com.cryptofolio.backend.application.port.in;

import com.cryptofolio.backend.application.dto.response.TransactionResponse;

import java.util.List;

public interface GetTransactionHistoryInputPort {

    List<TransactionResponse> execute(Long userId, Long portfolioId);
}
