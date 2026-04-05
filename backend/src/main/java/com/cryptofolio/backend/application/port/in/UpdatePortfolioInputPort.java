package com.cryptofolio.backend.application.port.in;

import com.cryptofolio.backend.application.dto.request.CreatePortfolioRequest;
import com.cryptofolio.backend.application.dto.response.PortfolioResponse;

public interface UpdatePortfolioInputPort {

    PortfolioResponse execute(Long userId, Long portfolioId, CreatePortfolioRequest request);
}
