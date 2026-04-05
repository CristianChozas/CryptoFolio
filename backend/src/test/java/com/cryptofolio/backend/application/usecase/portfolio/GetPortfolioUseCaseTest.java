package com.cryptofolio.backend.application.usecase.portfolio;

import com.cryptofolio.backend.application.dto.response.PortfolioResponse;
import com.cryptofolio.backend.application.mapper.PortfolioMapper;
import com.cryptofolio.backend.application.port.out.PortfolioRepository;
import com.cryptofolio.backend.domain.exception.PortfolioNotFoundException;
import com.cryptofolio.backend.domain.exception.UnauthorizedPortfolioAccessException;
import com.cryptofolio.backend.domain.model.Portfolio;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetPortfolioUseCaseTest {

    private final PortfolioRepository portfolioRepository = mock(PortfolioRepository.class);
    private final PortfolioMapper portfolioMapper = Mappers.getMapper(PortfolioMapper.class);

    private final GetPortfolioUseCase useCase = new GetPortfolioUseCase(portfolioRepository, portfolioMapper);

    @Test
    void shouldReturnPortfolioForOwner() {
        Portfolio portfolio = new Portfolio(15L, "Main Portfolio", "Long term", 7L,
                Instant.parse("2026-04-05T17:00:00Z"));

        when(portfolioRepository.findById(15L)).thenReturn(Optional.of(portfolio));

        PortfolioResponse response = useCase.execute(7L, 15L);

        assertThat(response.getId()).isEqualTo(15L);
        assertThat(response.getName()).isEqualTo("Main Portfolio");
        assertThat(response.getDescription()).isEqualTo("Long term");
        assertThat(response.getCreatedAt()).isEqualTo(Instant.parse("2026-04-05T17:00:00Z"));

        verify(portfolioRepository).findById(15L);
    }

    @Test
    void shouldRejectMissingPortfolio() {
        when(portfolioRepository.findById(15L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(7L, 15L))
                .isInstanceOf(PortfolioNotFoundException.class)
                .hasMessage("Portfolio not found with id: 15");
    }

    @Test
    void shouldRejectPortfolioOwnedByAnotherUser() {
        Portfolio portfolio = new Portfolio(15L, "Main Portfolio", "Long term", 99L,
                Instant.parse("2026-04-05T17:00:00Z"));

        when(portfolioRepository.findById(15L)).thenReturn(Optional.of(portfolio));

        assertThatThrownBy(() -> useCase.execute(7L, 15L))
                .isInstanceOf(UnauthorizedPortfolioAccessException.class)
                .hasMessage("User 7 is not authorized to access portfolio: 15");
    }
}
