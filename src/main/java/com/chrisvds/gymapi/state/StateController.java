package com.chrisvds.gymapi.state;

import java.time.Instant;
import java.util.Map;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.PAYLOAD_TOO_LARGE;

/**
 * Whole-blob backup for the SPA. GET pulls the stored state, PUT overwrites it.
 * No merge/conflict handling — last write wins. Single document until there's auth.
 */
@RestController
@RequestMapping("/api/state")
class StateController {

	private static final String KEY = "default";
	private static final int MAX_CHARS = 5_000_000; // ~5 MB of JSON text

	private final WorkoutStateRepository repo;
	private final ObjectMapper mapper;

	StateController(WorkoutStateRepository repo, ObjectMapper mapper) {
		this.repo = repo;
		this.mapper = mapper;
	}

	@GetMapping
	ResponseEntity<Map<String, Object>> get() {
		return repo.findById(KEY)
				.<ResponseEntity<Map<String, Object>>>map(s -> ResponseEntity.ok(Map.of(
						"updatedAt", s.getUpdatedAt().toString(),
						"payload", readTree(s.getPayload()))))
				.orElseGet(() -> ResponseEntity.noContent().build());
	}

	@PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@Transactional
	ResponseEntity<Map<String, String>> put(@RequestBody JsonNode payload) {
		String text = payload.toString();
		if (text.length() > MAX_CHARS) {
			throw new ResponseStatusException(PAYLOAD_TOO_LARGE, "state payload too large");
		}
		WorkoutState state = repo.findById(KEY).orElseGet(() -> new WorkoutState(KEY));
		state.setPayload(text);
		state.setUpdatedAt(Instant.now());
		repo.save(state);
		return ResponseEntity.ok(Map.of("updatedAt", state.getUpdatedAt().toString()));
	}

	private JsonNode readTree(String json) {
		try {
			return mapper.readTree(json);
		}
		catch (RuntimeException e) {
			throw new IllegalStateException("stored state is not valid JSON", e);
		}
	}
}
