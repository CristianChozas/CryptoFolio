package com.cryptofolio.backend.infrastructure.external;

import com.cryptofolio.backend.application.port.out.CryptoPriceProvider;
import com.cryptofolio.backend.domain.valueobject.Crypto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class StubCryptoPriceProvider implements CryptoPriceProvider {

    private static final BigDecimal DEFAULT_PRICE = BigDecimal.ONE;

    @Override
    public BigDecimal getCurrentPrice(Crypto crypto) {
        return DEFAULT_PRICE;
    }
}
