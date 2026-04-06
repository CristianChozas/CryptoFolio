package com.cryptofolio.backend.infrastructure.security;

import com.cryptofolio.backend.infrastructure.out.persistence.entity.UserEntity;
import com.cryptofolio.backend.infrastructure.out.persistence.repository.JpaUserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final List<GrantedAuthority> DEFAULT_AUTHORITIES = List.of(new SimpleGrantedAuthority("ROLE_USER"));

    private final JpaUserRepository jpaUserRepository;

    public UserDetailsServiceImpl(JpaUserRepository jpaUserRepository) {
        this.jpaUserRepository = jpaUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = jpaUserRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found for email: " + username));

        return User.withUsername(userEntity.getEmail())
                .password(userEntity.getPasswordHash())
                .authorities(DEFAULT_AUTHORITIES)
                .build();
    }
}
