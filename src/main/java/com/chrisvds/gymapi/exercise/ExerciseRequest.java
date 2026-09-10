package com.chrisvds.gymapi.exercise;

/**
 * Create or update payload. On create, {@code name} is required; {@code id} is
 * optional (the client may supply one so an offline-queued create is idempotent).
 * On PATCH, any null field is left unchanged.
 */
public record ExerciseRequest(
		String id,
		String name,
		String muscle,
		Equipment equipment) {
}
