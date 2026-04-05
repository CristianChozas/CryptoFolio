package com.cryptofolio.backend.application.port.in;

import com.cryptofolio.backend.application.dto.response.PortfolioResponse;

public interface GetPortfolioInputPort {

    PortfolioResponse execute(Long userId, Long portfolioId);
}
