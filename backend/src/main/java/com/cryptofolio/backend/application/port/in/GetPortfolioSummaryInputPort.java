package com.cryptofolio.backend.application.port.in;

import com.cryptofolio.backend.application.dto.response.PortfolioSummaryResponse;

public interface GetPortfolioSummaryInputPort {

    PortfolioSummaryResponse execute(Long userId, Long portfolioId);
}
