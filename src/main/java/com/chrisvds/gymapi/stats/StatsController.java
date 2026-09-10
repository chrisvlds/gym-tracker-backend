package com.chrisvds.gymapi.stats;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.chrisvds.gymapi.stats.StatsDtos.ExerciseHistory;

@RestController
class StatsController {

	private final StatsService service;

	StatsController(StatsService service) {
		this.service = service;
	}

	@GetMapping("/api/exercises/{id}/history")
	ExerciseHistory exerciseHistory(@PathVariable String id) {
		return service.exerciseHistory(id);
	}
}
