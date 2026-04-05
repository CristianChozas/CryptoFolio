package com.cryptofolio.backend.application.port.out;

import com.cryptofolio.backend.domain.valueobject.Crypto;

import java.math.BigDecimal;

public interface CryptoPriceProvider {

    BigDecimal getCurrentPrice(Crypto crypto);
}
