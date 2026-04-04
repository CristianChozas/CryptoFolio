package com.cryptofolio.backend.application.port.out;

import com.cryptofolio.backend.domain.model.User;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    void deleteById(Long id);
}
