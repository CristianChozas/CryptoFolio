package com.cryptofolio.backend.application.port.in;

import com.cryptofolio.backend.application.dto.request.CreatePortfolioRequest;
import com.cryptofolio.backend.application.dto.response.PortfolioResponse;

public interface CreatePortfolioInputPort {

    PortfolioResponse execute(Long userId, CreatePortfolioRequest request);
}
