package com.cryptofolio.backend.application.port.in;

public interface DeleteTransactionInputPort {

    void execute(Long userId, Long transactionId);
}
