package com.cryptofolio.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CryptofolioApplication {

	public static void main(String[] args) {
		SpringApplication.run(CryptofolioApplication.class, args);
	}
}
