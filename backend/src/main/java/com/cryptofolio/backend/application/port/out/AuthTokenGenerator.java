package com.cryptofolio.backend.application.port.out;

public interface AuthTokenGenerator {

    String generateToken(Long userId);
}
