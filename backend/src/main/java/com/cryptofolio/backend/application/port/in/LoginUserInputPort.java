package com.cryptofolio.backend.application.port.in;

import com.cryptofolio.backend.application.dto.request.LoginRequest;
import com.cryptofolio.backend.application.dto.response.AuthResponse;

public interface LoginUserInputPort {

    AuthResponse execute(LoginRequest request);
}
