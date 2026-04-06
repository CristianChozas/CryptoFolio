package com.cryptofolio.backend.infrastructure.security;

import com.cryptofolio.backend.infrastructure.out.persistence.entity.UserEntity;
import com.cryptofolio.backend.infrastructure.out.persistence.repository.JpaUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private JpaUserRepository jpaUserRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void givenPersistedUser_whenLoadingByEmail_thenReturnsSpringSecurityUser() {
        when(jpaUserRepository.findByEmail("cristian@example.com"))
                .thenReturn(Optional.of(new UserEntity(
                        1L,
                        "cristian",
                        "cristian@example.com",
                        "hashed-password",
                        Instant.parse("2026-04-06T16:30:00Z"))));

        UserDetails userDetails = userDetailsService.loadUserByUsername("cristian@example.com");

        assertThat(userDetails.getUsername()).isEqualTo("cristian@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("hashed-password");
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void givenUnknownEmail_whenLoadingByEmail_thenThrowsUsernameNotFound() {
        when(jpaUserRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found for email: missing@example.com");
    }
}
