package com.chrisvds.gymapi.program;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chrisvds.gymapi.program.ProgramDtos.NextDay;
import com.chrisvds.gymapi.program.ProgramDtos.ProgramDto;
import com.chrisvds.gymapi.program.ProgramDtos.ProgramRequest;

@RestController
@RequestMapping("/api/program")
class ProgramController {

	private final ProgramService service;

	ProgramController(ProgramService service) {
		this.service = service;
	}

	@GetMapping
	ProgramDto get() {
		return service.get();
	}

	@PutMapping
	ProgramDto put(@RequestBody ProgramRequest req) {
		return service.replace(req);
	}

	/** Suggested next day + per-exercise last performance and progression advice. */
	@GetMapping("/next")
	NextDay next() {
		return service.next();
	}
}
