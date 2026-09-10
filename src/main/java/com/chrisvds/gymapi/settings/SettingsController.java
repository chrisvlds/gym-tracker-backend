package com.chrisvds.gymapi.settings;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chrisvds.gymapi.settings.SettingsService.SettingsDto;
import com.chrisvds.gymapi.settings.SettingsService.SettingsRequest;

@RestController
@RequestMapping("/api/settings")
class SettingsController {

	private final SettingsService service;

	SettingsController(SettingsService service) {
		this.service = service;
	}

	@GetMapping
	SettingsDto get() {
		return service.get();
	}

	@PutMapping
	SettingsDto put(@RequestBody SettingsRequest req) {
		return service.update(req);
	}
}
