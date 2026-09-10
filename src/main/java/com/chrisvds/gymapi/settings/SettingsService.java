package com.chrisvds.gymapi.settings;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chrisvds.gymapi.identity.CurrentUser;

@Service
public class SettingsService {

	public record SettingsDto(String unit, int restSeconds) {
		static SettingsDto of(Settings s) {
			return new SettingsDto(s.getUnit(), s.getRestSeconds());
		}
	}

	public record SettingsRequest(String unit, Integer restSeconds) {
	}

	private final SettingsRepository repo;
	private final CurrentUser currentUser;

	SettingsService(SettingsRepository repo, CurrentUser currentUser) {
		this.repo = repo;
		this.currentUser = currentUser;
	}

	@Transactional
	public Settings entity(String owner) {
		return repo.findByOwner(owner).orElseGet(() -> {
			Settings s = new Settings(owner);
			s.setOwner(owner);
			return repo.save(s);
		});
	}

	/** Just the unit, for callers that only need it (progression, program/next). */
	@Transactional
	public String unit(String owner) {
		return entity(owner).getUnit();
	}

	@Transactional(readOnly = true)
	public SettingsDto get() {
		return SettingsDto.of(entity(currentUser.owner()));
	}

	@Transactional
	public SettingsDto update(SettingsRequest req) {
		Settings s = entity(currentUser.owner());
		if (req.unit() != null) {
			s.setUnit(req.unit());
		}
		if (req.restSeconds() != null) {
			s.setRestSeconds(req.restSeconds());
		}
		return SettingsDto.of(repo.save(s));
	}
}
