package com.chrisvds.gymapi.web;

import java.time.Instant;
import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PingController {

	private final String version;

	public PingController(ObjectProvider<BuildProperties> buildProperties) {
		BuildProperties bp = buildProperties.getIfAvailable();
		this.version = bp != null ? bp.getVersion() : "dev";
	}

	@GetMapping("/ping")
	public Map<String, Object> ping() {
		return Map.of(
				"status", "ok",
				"service", "gym-api",
				"version", version,
				"time", Instant.now().toString());
	}
}
