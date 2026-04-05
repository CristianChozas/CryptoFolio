package com.cryptofolio.backend.application.port.in;

import com.cryptofolio.backend.application.dto.request.AddTransactionRequest;
import com.cryptofolio.backend.application.dto.response.TransactionResponse;

public interface AddTransactionInputPort {

    TransactionResponse execute(Long userId, AddTransactionRequest request);
}
