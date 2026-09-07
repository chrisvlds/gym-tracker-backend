package com.chrisvds.gymapi.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS for the browser SPA. Origins are configurable so local dev and the
 * deployed frontend both work; set gym-api.cors.allowed-origins in the prod
 * profile / env (GYM_API_CORS_ALLOWED_ORIGINS).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	private final List<String> allowedOrigins;

	public WebConfig(
			@Value("${gym-api.cors.allowed-origins:http://localhost:5173}") List<String> allowedOrigins) {
		this.allowedOrigins = allowedOrigins;
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**")
				.allowedOrigins(allowedOrigins.toArray(String[]::new))
				.allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
				.allowedHeaders("*")
				.maxAge(3600);
	}
}
