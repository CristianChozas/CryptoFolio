package com.cryptofolio.backend.infrastructure.in.web.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

	private final String applicationName;

	public RootController(@Value("${spring.application.name}") String applicationName) {
		this.applicationName = applicationName;
	}

	@GetMapping("/")
	public Map<String, String> getRoot() {
		return Map.of(
			"service", applicationName,
			"status", "UP",
			"health", "/api/health",
			"docs", "/swagger-ui/index.html",
			"openapi", "/v3/api-docs");
	}
}
