package com.cryptofolio.backend.infrastructure.in.web.controller;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cryptofolio.backend.infrastructure.in.web.response.HealthResponse;

@RestController
@RequestMapping("/api/health")
public class HealthController {

	private final String applicationName;

	public HealthController(@Value("${spring.application.name}") String applicationName) {
		this.applicationName = applicationName;
	}

	@GetMapping
	public HealthResponse getHealth() {
		return new HealthResponse("UP", applicationName, Instant.now());
	}
}
