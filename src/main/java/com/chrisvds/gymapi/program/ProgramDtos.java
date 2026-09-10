package com.chrisvds.gymapi.program;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.chrisvds.gymapi.exercise.Equipment;
import com.chrisvds.gymapi.progression.Suggestion;

/** Request/response shapes for the program API. */
public final class ProgramDtos {

	private ProgramDtos() {
	}

	public record SlotDto(
			String exerciseId,
			int targetSets,
			int repMin,
			int repMax,
			Double increment) {

		static SlotDto of(ProgramSlot s) {
			return new SlotDto(s.getExerciseId(), s.getTargetSets(), s.getRepMin(), s.getRepMax(), s.getIncrement());
		}
	}

	public record DayDto(String key, String name, List<SlotDto> slots) {

		static DayDto of(ProgramDay d) {
			return new DayDto(d.getKey(), d.getName(), d.getSlots().stream().map(SlotDto::of).toList());
		}
	}

	public record ProgramDto(
			String id,
			String name,
			List<String> cycle,
			List<DayDto> days,
			Instant updatedAt) {

		static ProgramDto of(Program p) {
			return new ProgramDto(p.getId(), p.getName(), List.copyOf(p.getCycle()),
					p.getDays().stream().map(DayDto::of).toList(), p.getUpdatedAt());
		}
	}

	public record ProgramRequest(String name, List<String> cycle, List<DayDto> days) {
	}

	// --- GET /api/program/next -------------------------------------------------

	public record PlannedSet(double weight, int reps, boolean done) {
	}

	public record PlannedExercise(
			String exerciseId,
			String name,
			String muscle,
			Equipment equipment,
			int targetSets,
			int repMin,
			int repMax,
			LocalDate lastWorkoutDate,
			List<PlannedSet> lastPerformance,
			Suggestion suggestion) {
	}

	public record NextDay(
			String dayKey,
			String dayName,
			int cyclePosition,
			LocalDate lastWorkoutDate,
			String lastDayKey,
			List<PlannedExercise> exercises) {
	}
}
