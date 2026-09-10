package com.chrisvds.gymapi.program;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chrisvds.gymapi.exercise.Exercise;
import com.chrisvds.gymapi.exercise.ExerciseRepository;
import com.chrisvds.gymapi.exercise.ExerciseService;
import com.chrisvds.gymapi.identity.CurrentUser;
import com.chrisvds.gymapi.program.ProgramDtos.DayDto;
import com.chrisvds.gymapi.program.ProgramDtos.NextDay;
import com.chrisvds.gymapi.program.ProgramDtos.PlannedExercise;
import com.chrisvds.gymapi.program.ProgramDtos.PlannedSet;
import com.chrisvds.gymapi.program.ProgramDtos.ProgramDto;
import com.chrisvds.gymapi.program.ProgramDtos.ProgramRequest;
import com.chrisvds.gymapi.program.ProgramDtos.SlotDto;
import com.chrisvds.gymapi.progression.ProgressionService;
import com.chrisvds.gymapi.progression.SetPerf;
import com.chrisvds.gymapi.progression.Suggestion;
import com.chrisvds.gymapi.settings.SettingsService;
import com.chrisvds.gymapi.workout.Workout;
import com.chrisvds.gymapi.workout.WorkoutEntry;
import com.chrisvds.gymapi.workout.WorkoutRepository;
import com.chrisvds.gymapi.workout.WorkoutSet;

@Service
public class ProgramService {

	private final ProgramRepository repo;
	private final ExerciseRepository exercises;
	private final ExerciseService exerciseService;
	private final WorkoutRepository workouts;
	private final ProgressionService progression;
	private final SettingsService settings;
	private final CurrentUser currentUser;

	ProgramService(ProgramRepository repo, ExerciseRepository exercises, ExerciseService exerciseService,
			WorkoutRepository workouts, ProgressionService progression, SettingsService settings,
			CurrentUser currentUser) {
		this.repo = repo;
		this.exercises = exercises;
		this.exerciseService = exerciseService;
		this.workouts = workouts;
		this.progression = progression;
		this.settings = settings;
		this.currentUser = currentUser;
	}

	@Transactional
	public Program ensureSeeded(String owner) {
		return repo.findByOwner(owner).orElseGet(() -> {
			exerciseService.ensureSeeded(owner);
			return repo.save(ProgramDefaults.build(UUID.randomUUID().toString(), owner));
		});
	}

	@Transactional
	public ProgramDto get() {
		return ProgramDto.of(ensureSeeded(currentUser.owner()));
	}

	@Transactional
	public ProgramDto replace(ProgramRequest req) {
		String owner = currentUser.owner();
		Program program = ensureSeeded(owner);

		if (req.name() != null && !req.name().isBlank()) {
			program.setName(req.name().trim());
		}
		if (req.cycle() != null) {
			program.getCycle().clear();
			req.cycle().stream().filter(k -> k != null && !k.isBlank())
					.forEach(k -> program.getCycle().add(k.trim()));
		}
		if (req.days() != null) {
			program.getDays().clear();
			for (DayDto d : req.days()) {
				ProgramDay day = new ProgramDay(d.key().trim(), d.name() == null ? d.key() : d.name().trim());
				if (d.slots() != null) {
					for (SlotDto s : d.slots()) {
						ProgramSlot slot = new ProgramSlot(s.exerciseId().trim());
						slot.setTargetSets(clamp(s.targetSets(), 1, 10, 3));
						slot.setRepMin(clamp(s.repMin(), 1, 50, 6));
						slot.setRepMax(clamp(s.repMax(), Math.max(1, s.repMin()), 50, 8));
						slot.setIncrement(s.increment());
						day.getSlots().add(slot);
					}
				}
				program.getDays().add(day);
			}
		}
		program.touch();
		return ProgramDto.of(repo.save(program));
	}

	/** The guidance endpoint: which day is next, and how to progress each exercise. */
	@Transactional
	public NextDay next() {
		String owner = currentUser.owner();
		Program program = ensureSeeded(owner);
		String unit = settings.unit(owner);

		List<String> cycle = program.getCycle();
		if (cycle.isEmpty()) {
			cycle = ProgramDefaults.CYCLE;
		}

		Workout lastProgramWorkout = workouts
				.findFirstByOwnerAndProgramDayKeyIsNotNullOrderByDateDescCreatedAtDesc(owner)
				.orElse(null);
		int nextIndex = nextCycleIndex(owner, cycle);
		String dayKey = cycle.get(nextIndex);

		ProgramDay day = program.day(dayKey);
		if (day == null) {
			day = program.getDays().isEmpty() ? null : program.getDays().get(0);
			dayKey = day == null ? dayKey : day.getKey();
		}

		List<PlannedExercise> planned = new ArrayList<>();
		if (day != null) {
			Map<String, Exercise> byId = new HashMap<>();
			exercises.findByOwnerOrderByNameAsc(owner).forEach(e -> byId.put(e.getId(), e));

			for (ProgramSlot slot : day.getSlots()) {
				Exercise ex = byId.get(slot.getExerciseId());
				double increment = slot.getIncrement() != null
						? slot.getIncrement()
						: (ex != null ? ex.getEquipment().defaultIncrement(unit) : 5.0);

				List<WorkoutSet> lastSets = lastSetsFor(owner, slot.getExerciseId());
				List<SetPerf> perf = lastSets.stream()
						.map(s -> new SetPerf(s.getWeight(), s.getReps(), s.isDone())).toList();
				Suggestion suggestion = progression.suggest(
						perf, slot.getRepMin(), slot.getRepMax(), increment, unit);

				Workout lastWorkout = lastWorkoutWith(owner, slot.getExerciseId());
				planned.add(new PlannedExercise(
						slot.getExerciseId(),
						ex != null ? ex.getName() : "Unknown exercise",
						ex != null ? ex.getMuscle() : "Other",
						ex != null ? ex.getEquipment() : null,
						slot.getTargetSets(), slot.getRepMin(), slot.getRepMax(),
						lastWorkout != null ? lastWorkout.getDate() : null,
						lastSets.stream().map(s -> new PlannedSet(s.getWeight(), s.getReps(), s.isDone())).toList(),
						suggestion));
			}
		}

		return new NextDay(
				dayKey,
				day != null ? day.getName() : dayKey,
				nextIndex,
				lastProgramWorkout != null ? lastProgramWorkout.getDate() : null,
				lastProgramWorkout != null ? lastProgramWorkout.getProgramDayKey() : null,
				planned);
	}

	/**
	 * Where we are in the cycle: match the tail of recent program days against the
	 * cycle (handles the repeated Push/Pull and the occasional skipped day), then
	 * return the index that comes next.
	 */
	private int nextCycleIndex(String owner, List<String> cycle) {
		int len = cycle.size();
		List<String> recent = recentProgramDays(owner, len);
		if (recent.isEmpty()) {
			return 0;
		}

		int bestPos = -1;
		int bestMatch = 0;
		for (int p = 0; p < len; p++) {
			int match = 0;
			for (int k = 0; k < recent.size(); k++) {
				int cycleIdx = Math.floorMod(p - k, len);
				if (recent.get(recent.size() - 1 - k).equals(cycle.get(cycleIdx))) {
					match++;
				} else {
					break;
				}
			}
			if (match > bestMatch) {
				bestMatch = match;
				bestPos = p;
			}
		}
		return bestPos < 0 ? 0 : Math.floorMod(bestPos + 1, len);
	}

	private List<String> recentProgramDays(String owner, int limit) {
		List<String> keys = new ArrayList<>();
		for (Workout w : workouts.findByOwnerOrderByDateDescCreatedAtDesc(owner)) {
			if (w.getProgramDayKey() != null) {
				keys.add(w.getProgramDayKey());
				if (keys.size() >= limit) {
					break;
				}
			}
		}
		java.util.Collections.reverse(keys); // oldest → newest
		return keys;
	}

	private Workout lastWorkoutWith(String owner, String exerciseId) {
		List<Workout> history = workouts.findHistoryForExercise(owner, exerciseId);
		return history.isEmpty() ? null : history.get(history.size() - 1);
	}

	private List<WorkoutSet> lastSetsFor(String owner, String exerciseId) {
		Workout last = lastWorkoutWith(owner, exerciseId);
		if (last == null) {
			return List.of();
		}
		return last.getEntries().stream()
				.filter(e -> e.getExerciseId().equals(exerciseId))
				.findFirst()
				.map(WorkoutEntry::getSets)
				.orElse(List.of());
	}

	private static int clamp(int v, int min, int max, int fallback) {
		if (v < min || v > max) {
			return Math.min(Math.max(v <= 0 ? fallback : v, min), max);
		}
		return v;
	}
}
