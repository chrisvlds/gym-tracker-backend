package com.chrisvds.gymapi.imports;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chrisvds.gymapi.imports.ImportService.ImportResult;

@RestController
class ImportController {

	private final ImportService service;

	ImportController(ImportService service) {
		this.service = service;
	}

	/** One-time migration of the old {@code gym-tracker:v1} localStorage blob. */
	@PostMapping("/api/import")
	ImportResult importV1(@RequestBody V1Blob blob, @RequestParam(defaultValue = "false") boolean force) {
		return service.importV1(blob, force);
	}
}
