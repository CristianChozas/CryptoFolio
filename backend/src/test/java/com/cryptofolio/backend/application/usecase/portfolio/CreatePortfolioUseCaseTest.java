package com.cryptofolio.backend.application.usecase.portfolio;

import com.cryptofolio.backend.application.dto.request.CreatePortfolioRequest;
import com.cryptofolio.backend.application.dto.response.PortfolioResponse;
import com.cryptofolio.backend.application.mapper.PortfolioMapper;
import com.cryptofolio.backend.application.port.out.PortfolioRepository;
import com.cryptofolio.backend.application.port.out.UserRepository;
import com.cryptofolio.backend.domain.exception.UserNotFoundException;
import com.cryptofolio.backend.domain.model.Portfolio;
import com.cryptofolio.backend.domain.model.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreatePortfolioUseCaseTest {

    private static final Instant NOW = Instant.parse("2026-04-05T17:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PortfolioRepository portfolioRepository = mock(PortfolioRepository.class);
    private final PortfolioMapper portfolioMapper = Mappers.getMapper(PortfolioMapper.class);

    private final CreatePortfolioUseCase useCase = new CreatePortfolioUseCase(
            userRepository,
            portfolioRepository,
            portfolioMapper,
            FIXED_CLOCK);

    @Test
    void shouldCreatePortfolioAndReturnResponse() {
        CreatePortfolioRequest request = new CreatePortfolioRequest("Main Portfolio", "Long term");
        User existingUser = new User(7L, "cristian", "cristian@example.com", "hashed-password",
                Instant.parse("2026-04-05T12:00:00Z"));
        Portfolio savedPortfolio = new Portfolio(15L, "Main Portfolio", "Long term", 7L, NOW);

        when(userRepository.findById(7L)).thenReturn(Optional.of(existingUser));
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(savedPortfolio);

        PortfolioResponse response = useCase.execute(7L, request);

        assertThat(response.getId()).isEqualTo(15L);
        assertThat(response.getName()).isEqualTo("Main Portfolio");
        assertThat(response.getDescription()).isEqualTo("Long term");
        assertThat(response.getCreatedAt()).isEqualTo(NOW);

        verify(userRepository).findById(7L);
        verify(portfolioRepository).save(any(Portfolio.class));
    }

    @Test
    void shouldRejectUnknownUser() {
        CreatePortfolioRequest request = new CreatePortfolioRequest("Main Portfolio", "Long term");

        when(userRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(7L, request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: 7");

        verify(portfolioRepository, never()).save(any());
    }

    @Test
    void shouldUseClockTimestampWhenCreatingPortfolio() {
        CreatePortfolioRequest request = new CreatePortfolioRequest("Main Portfolio", "Long term");
        User existingUser = new User(7L, "cristian", "cristian@example.com", "hashed-password",
                Instant.parse("2026-04-05T12:00:00Z"));
        ArgumentCaptor<Portfolio> portfolioCaptor = ArgumentCaptor.forClass(Portfolio.class);

        when(userRepository.findById(7L)).thenReturn(Optional.of(existingUser));
        when(portfolioRepository.save(any(Portfolio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PortfolioResponse response = useCase.execute(7L, request);

        assertThat(response.getName()).isEqualTo("Main Portfolio");
        verify(portfolioRepository).save(portfolioCaptor.capture());

        Portfolio portfolioToSave = portfolioCaptor.getValue();
        assertThat(portfolioToSave.getId()).isNull();
        assertThat(portfolioToSave.getName()).isEqualTo("Main Portfolio");
        assertThat(portfolioToSave.getDescription()).isEqualTo("Long term");
        assertThat(portfolioToSave.getUserId()).isEqualTo(7L);
        assertThat(portfolioToSave.getCreatedAt()).isEqualTo(NOW);
    }
}
