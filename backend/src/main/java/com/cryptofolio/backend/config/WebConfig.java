package com.cryptofolio.backend.config;

import com.cryptofolio.backend.infrastructure.logging.HttpRequestLoggingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	private final HttpRequestLoggingInterceptor httpRequestLoggingInterceptor;

	public WebConfig(HttpRequestLoggingInterceptor httpRequestLoggingInterceptor) {
		this.httpRequestLoggingInterceptor = httpRequestLoggingInterceptor;
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**")
			.allowedOrigins("http://localhost:4200")
			.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
			.allowedHeaders("*");
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(httpRequestLoggingInterceptor)
			.addPathPatterns("/api/**");
	}
}
