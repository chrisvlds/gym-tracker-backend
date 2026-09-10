package com.chrisvds.gymapi.settings;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingsRepository extends JpaRepository<Settings, String> {

	Optional<Settings> findByOwner(String owner);
}
