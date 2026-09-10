package com.chrisvds.gymapi.exercise;

import java.time.Instant;

public record ExerciseDto(
		String id,
		String name,
		String muscle,
		Equipment equipment,
		boolean custom,
		boolean archived,
		Instant updatedAt) {

	static ExerciseDto of(Exercise e) {
		return new ExerciseDto(e.getId(), e.getName(), e.getMuscle(), e.getEquipment(),
				e.isCustom(), e.isArchived(), e.getUpdatedAt());
	}
}
