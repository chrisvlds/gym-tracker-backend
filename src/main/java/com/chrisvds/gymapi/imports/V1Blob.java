package com.chrisvds.gymapi.imports;

import java.util.List;

/**
 * The shape of the old frontend's single {@code gym-tracker:v1} localStorage
 * blob, accepted by {@code POST /api/import} as the one-time migration path.
 */
public record V1Blob(
		Integer version,
		Settings settings,
		List<Exercise> customExercises,
		List<Workout> workouts) {

	public record Settings(String unit) {
	}

	public record Exercise(String id, String name, String muscle) {
	}

	public record Workout(String id, String date, String note, List<Entry> entries) {
	}

	public record Entry(String id, String exerciseId, List<Set> sets) {
	}

	public record Set(String id, Double weight, Integer reps, Boolean done) {
	}
}
