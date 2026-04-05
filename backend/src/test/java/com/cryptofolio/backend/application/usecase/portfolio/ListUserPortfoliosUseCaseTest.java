package com.cryptofolio.backend.application.usecase.portfolio;

import com.cryptofolio.backend.application.dto.response.PortfolioResponse;
import com.cryptofolio.backend.application.mapper.PortfolioMapper;
import com.cryptofolio.backend.application.port.out.PortfolioRepository;
import com.cryptofolio.backend.domain.model.Portfolio;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ListUserPortfoliosUseCaseTest {

    private final PortfolioRepository portfolioRepository = mock(PortfolioRepository.class);
    private final PortfolioMapper portfolioMapper = Mappers.getMapper(PortfolioMapper.class);

    private final ListUserPortfoliosUseCase useCase = new ListUserPortfoliosUseCase(portfolioRepository, portfolioMapper);

    @Test
    void shouldReturnEmptyListWhenUserHasNoPortfolios() {
        when(portfolioRepository.findByUserId(7L)).thenReturn(List.of());

        List<PortfolioResponse> response = useCase.execute(7L);

        assertThat(response).isEmpty();
        verify(portfolioRepository).findByUserId(7L);
    }

    @Test
    void shouldReturnSinglePortfolioForUser() {
        Portfolio portfolio = new Portfolio(15L, "Main Portfolio", "Long term", 7L,
                Instant.parse("2026-04-05T17:00:00Z"));

        when(portfolioRepository.findByUserId(7L)).thenReturn(List.of(portfolio));

        List<PortfolioResponse> response = useCase.execute(7L);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getId()).isEqualTo(15L);
        assertThat(response.get(0).getName()).isEqualTo("Main Portfolio");
    }

    @Test
    void shouldReturnMultiplePortfoliosForUser() {
        Portfolio first = new Portfolio(15L, "Main Portfolio", "Long term", 7L,
                Instant.parse("2026-04-05T17:00:00Z"));
        Portfolio second = new Portfolio(16L, "Trading Portfolio", "Swing trades", 7L,
                Instant.parse("2026-04-06T17:00:00Z"));

        when(portfolioRepository.findByUserId(7L)).thenReturn(List.of(first, second));

        List<PortfolioResponse> response = useCase.execute(7L);

        assertThat(response).hasSize(2);
        assertThat(response)
                .extracting(PortfolioResponse::getId)
                .containsExactly(15L, 16L);
    }
}
