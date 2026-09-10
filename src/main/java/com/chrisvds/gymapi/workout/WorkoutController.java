package com.chrisvds.gymapi.workout;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.chrisvds.gymapi.workout.WorkoutDtos.CreateRequest;
import com.chrisvds.gymapi.workout.WorkoutDtos.EntriesRequest;
import com.chrisvds.gymapi.workout.WorkoutDtos.PatchRequest;
import com.chrisvds.gymapi.workout.WorkoutDtos.WorkoutDto;
import com.chrisvds.gymapi.workout.WorkoutDtos.WorkoutSummaryDto;

@RestController
@RequestMapping("/api/workouts")
class WorkoutController {

	private final WorkoutService service;

	WorkoutController(WorkoutService service) {
		this.service = service;
	}

	@GetMapping
	List<WorkoutSummaryDto> list(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(required = false) Integer limit) {
		return service.list(from, to, limit);
	}

	@GetMapping("/{id}")
	WorkoutDto get(@PathVariable String id) {
		return service.get(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	WorkoutDto create(@RequestBody CreateRequest req) {
		return service.create(req);
	}

	@PatchMapping("/{id}")
	WorkoutDto patch(@PathVariable String id, @RequestBody PatchRequest req) {
		return service.patch(id, req);
	}

	@PutMapping("/{id}/entries")
	WorkoutDto replaceEntries(@PathVariable String id, @RequestBody EntriesRequest req) {
		return service.replaceEntries(id, req);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void delete(@PathVariable String id) {
		service.delete(id);
	}
}
