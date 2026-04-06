package com.cryptofolio.backend.infrastructure.security;

import com.cryptofolio.backend.infrastructure.out.persistence.repository.JpaUserRepository;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class AuthenticatedUserResolver {

    private final JpaUserRepository jpaUserRepository;

    public AuthenticatedUserResolver(JpaUserRepository jpaUserRepository) {
        this.jpaUserRepository = jpaUserRepository;
    }

    public Long resolveUserId(Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("Authenticated principal is required");
        }

        String email = principal.getName();
        return jpaUserRepository.findByEmail(email)
                .map(user -> user.getId())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found for email: " + email));
    }
}
