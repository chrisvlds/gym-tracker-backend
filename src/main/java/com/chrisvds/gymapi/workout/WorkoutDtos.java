package com.chrisvds.gymapi.workout;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.chrisvds.gymapi.common.Lifts;

/** Request/response shapes for the workout API. */
public final class WorkoutDtos {

	private WorkoutDtos() {
	}

	public record SetDto(double weight, int reps, boolean done) {
	}

	public record EntryDto(String exerciseId, List<SetDto> sets) {
	}

	public record WorkoutDto(
			String id,
			LocalDate date,
			String note,
			String programDayKey,
			Instant updatedAt,
			List<EntryDto> entries) {

		static WorkoutDto of(Workout w) {
			List<EntryDto> entries = w.getEntries().stream()
					.map(e -> new EntryDto(e.getExerciseId(), e.getSets().stream()
							.map(s -> new SetDto(s.getWeight(), s.getReps(), s.isDone()))
							.toList()))
					.toList();
			return new WorkoutDto(w.getId(), w.getDate(), w.getNote(),
					w.getProgramDayKey(), w.getUpdatedAt(), entries);
		}
	}

	public record WorkoutSummaryDto(
			String id,
			LocalDate date,
			String note,
			String programDayKey,
			List<String> exerciseIds,
			int sets,
			double volume,
			Instant updatedAt) {

		static WorkoutSummaryDto of(Workout w) {
			int sets = 0;
			double volume = 0;
			for (WorkoutEntry e : w.getEntries()) {
				for (WorkoutSet s : e.getSets()) {
					sets++;
					volume += Lifts.setVolume(s.getWeight(), s.getReps());
				}
			}
			return new WorkoutSummaryDto(w.getId(), w.getDate(), w.getNote(), w.getProgramDayKey(),
					w.getEntries().stream().map(WorkoutEntry::getExerciseId).toList(),
					sets, Lifts.round(volume, 1), w.getUpdatedAt());
		}
	}

	public record CreateRequest(
			String id,
			LocalDate date,
			String note,
			String programDayKey,
			List<EntryDto> entries) {
	}

	public record PatchRequest(
			LocalDate date,
			String note,
			String programDayKey,
			Instant expectedUpdatedAt) {
	}

	public record EntriesRequest(
			Instant expectedUpdatedAt,
			List<EntryDto> entries) {
	}
}
