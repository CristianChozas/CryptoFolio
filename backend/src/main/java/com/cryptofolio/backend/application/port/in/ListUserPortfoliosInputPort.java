package com.cryptofolio.backend.application.port.in;

import com.cryptofolio.backend.application.dto.response.PortfolioResponse;

import java.util.List;

public interface ListUserPortfoliosInputPort {

    List<PortfolioResponse> execute(Long userId);
}
