package com.cryptofolio.backend.application.port.in;

import com.cryptofolio.backend.application.dto.response.PortfolioOverviewResponse;

public interface GetPortfolioOverviewInputPort {

    PortfolioOverviewResponse execute(Long userId);
}
