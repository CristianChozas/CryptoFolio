package com.cryptofolio.backend.application.usecase.portfolio;

import com.cryptofolio.backend.application.dto.request.CreatePortfolioRequest;
import com.cryptofolio.backend.application.dto.response.PortfolioResponse;
import com.cryptofolio.backend.application.mapper.PortfolioMapper;
import com.cryptofolio.backend.application.port.out.PortfolioRepository;
import com.cryptofolio.backend.domain.exception.PortfolioNotFoundException;
import com.cryptofolio.backend.domain.exception.UnauthorizedPortfolioAccessException;
import com.cryptofolio.backend.domain.model.Portfolio;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdatePortfolioUseCaseTest {

    private final PortfolioRepository portfolioRepository = mock(PortfolioRepository.class);
    private final PortfolioMapper portfolioMapper = Mappers.getMapper(PortfolioMapper.class);

    private final UpdatePortfolioUseCase useCase = new UpdatePortfolioUseCase(portfolioRepository, portfolioMapper);

    @Test
    void shouldUpdateOwnedPortfolioAndReturnResponse() {
        Portfolio existing = new Portfolio(15L, "Main Portfolio", "Long term", 7L,
                Instant.parse("2026-04-05T17:00:00Z"));
        CreatePortfolioRequest request = new CreatePortfolioRequest("Updated Portfolio", "Updated description");
        Portfolio saved = new Portfolio(15L, "Updated Portfolio", "Updated description", 7L,
                Instant.parse("2026-04-05T17:00:00Z"));

        when(portfolioRepository.findById(15L)).thenReturn(Optional.of(existing));
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(saved);

        PortfolioResponse response = useCase.execute(7L, 15L, request);

        assertThat(response.getId()).isEqualTo(15L);
        assertThat(response.getName()).isEqualTo("Updated Portfolio");
        assertThat(response.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void shouldRejectMissingPortfolio() {
        when(portfolioRepository.findById(15L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(7L, 15L, new CreatePortfolioRequest("Updated Portfolio", "Desc")))
                .isInstanceOf(PortfolioNotFoundException.class)
                .hasMessage("Portfolio not found with id: 15");

        verify(portfolioRepository, never()).save(any());
    }

    @Test
    void shouldRejectPortfolioOwnedByAnotherUser() {
        Portfolio existing = new Portfolio(15L, "Main Portfolio", "Long term", 99L,
                Instant.parse("2026-04-05T17:00:00Z"));
        when(portfolioRepository.findById(15L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> useCase.execute(7L, 15L, new CreatePortfolioRequest("Updated Portfolio", "Desc")))
                .isInstanceOf(UnauthorizedPortfolioAccessException.class)
                .hasMessage("User 7 is not authorized to access portfolio: 15");

        verify(portfolioRepository, never()).save(any());
    }

    @Test
    void shouldUpdateOnlyDescriptionWhenNameStaysTheSame() {
        Portfolio existing = new Portfolio(15L, "Main Portfolio", "Long term", 7L,
                Instant.parse("2026-04-05T17:00:00Z"));
        CreatePortfolioRequest request = new CreatePortfolioRequest("Main Portfolio", "Updated description");
        ArgumentCaptor<Portfolio> portfolioCaptor = ArgumentCaptor.forClass(Portfolio.class);

        when(portfolioRepository.findById(15L)).thenReturn(Optional.of(existing));
        when(portfolioRepository.save(any(Portfolio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PortfolioResponse response = useCase.execute(7L, 15L, request);

        assertThat(response.getName()).isEqualTo("Main Portfolio");
        assertThat(response.getDescription()).isEqualTo("Updated description");
        verify(portfolioRepository).save(portfolioCaptor.capture());
        assertThat(portfolioCaptor.getValue().getDescription()).isEqualTo("Updated description");
    }
}
