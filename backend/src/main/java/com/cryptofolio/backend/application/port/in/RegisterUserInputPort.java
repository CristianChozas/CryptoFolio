package com.cryptofolio.backend.application.port.in;

import com.cryptofolio.backend.application.dto.request.RegisterUserRequest;
import com.cryptofolio.backend.application.dto.response.AuthResponse;

public interface RegisterUserInputPort {

    AuthResponse execute(RegisterUserRequest request);
}
