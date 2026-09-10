package com.chrisvds.gymapi.exercise;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exercises")
class ExerciseController {

	private final ExerciseService service;

	ExerciseController(ExerciseService service) {
		this.service = service;
	}

	@GetMapping
	List<ExerciseDto> list(@RequestParam(defaultValue = "false") boolean includeArchived) {
		return service.list(includeArchived);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	ExerciseDto create(@RequestBody ExerciseRequest req) {
		return service.create(req);
	}

	@PatchMapping("/{id}")
	ExerciseDto patch(@PathVariable String id, @RequestBody ExerciseRequest req) {
		return service.patch(id, req);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void delete(@PathVariable String id) {
		service.delete(id);
	}
}
