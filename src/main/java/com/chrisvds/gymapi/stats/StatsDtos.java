package com.chrisvds.gymapi.stats;

import java.time.LocalDate;
import java.util.List;

public final class StatsDtos {

	private StatsDtos() {
	}

	public record SetView(double weight, int reps, boolean done) {
	}

	public record Session(
			LocalDate date,
			String workoutId,
			String programDayKey,
			List<SetView> sets,
			double topWeight,
			int topReps,
			double best1RM,
			double volume) {
	}

	public record PersonalRecord(LocalDate date, String workoutId, double weight, int reps, double e1RM) {
	}

	public record ExerciseHistory(
			String exerciseId,
			String name,
			int sessionCount,
			PersonalRecord personalRecord,
			List<Session> sessions) {
	}
}
