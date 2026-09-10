package com.chrisvds.gymapi.imports;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chrisvds.gymapi.exercise.Equipment;
import com.chrisvds.gymapi.exercise.Exercise;
import com.chrisvds.gymapi.exercise.ExerciseRepository;
import com.chrisvds.gymapi.exercise.ExerciseService;
import com.chrisvds.gymapi.identity.CurrentUser;
import com.chrisvds.gymapi.program.ProgramService;
import com.chrisvds.gymapi.settings.Settings;
import com.chrisvds.gymapi.settings.SettingsService;
import com.chrisvds.gymapi.workout.Workout;
import com.chrisvds.gymapi.workout.WorkoutEntry;
import com.chrisvds.gymapi.workout.WorkoutRepository;
import com.chrisvds.gymapi.workout.WorkoutSet;

@Service
public class ImportService {

	public record ImportResult(boolean imported, String reason, int workouts, int exercises) {
	}

	private final ExerciseRepository exercises;
	private final ExerciseService exerciseService;
	private final WorkoutRepository workouts;
	private final SettingsService settingsService;
	private final ProgramService programService;
	private final CurrentUser currentUser;

	ImportService(ExerciseRepository exercises, ExerciseService exerciseService, WorkoutRepository workouts,
			SettingsService settingsService, ProgramService programService, CurrentUser currentUser) {
		this.exercises = exercises;
		this.exerciseService = exerciseService;
		this.workouts = workouts;
		this.settingsService = settingsService;
		this.programService = programService;
		this.currentUser = currentUser;
	}

	@Transactional
	public ImportResult importV1(V1Blob blob, boolean force) {
		String owner = currentUser.owner();
		exerciseService.ensureSeeded(owner);
		programService.ensureSeeded(owner);

		boolean hasData = !workouts.findByOwnerOrderByDateDescCreatedAtDesc(owner).isEmpty();
		if (hasData && !force) {
			return new ImportResult(false, "owner already has workouts; pass ?force=true to merge", 0, 0);
		}

		if (blob.settings() != null && blob.settings().unit() != null) {
			Settings s = settingsService.entity(owner);
			s.setUnit(blob.settings().unit());
		}

		int exerciseCount = 0;
		for (V1Blob.Exercise ce : nullSafe(blob.customExercises())) {
			if (ce.id() == null || ce.name() == null) {
				continue;
			}
			if (exercises.findByIdAndOwner(ce.id(), owner).isPresent()) {
				continue;
			}
			Exercise e = new Exercise(ce.id());
			e.setOwner(owner);
			e.setName(ce.name());
			e.setMuscle(ce.muscle() == null ? "Other" : ce.muscle());
			e.setEquipment(Equipment.OTHER);
			e.setCustom(true);
			exercises.save(e);
			exerciseCount++;
		}

		int workoutCount = 0;
		for (V1Blob.Workout vw : nullSafe(blob.workouts())) {
			if (vw.id() == null || vw.date() == null) {
				continue;
			}
			Workout w = workouts.findByIdAndOwner(vw.id(), owner).orElseGet(() -> {
				Workout fresh = new Workout(vw.id());
				fresh.setOwner(owner);
				return fresh;
			});
			w.setDate(LocalDate.parse(vw.date()));
			w.setNote(vw.note());
			w.getEntries().clear();
			for (V1Blob.Entry ve : nullSafe(vw.entries())) {
				if (ve.exerciseId() == null) {
					continue;
				}
				WorkoutEntry entry = new WorkoutEntry(ve.exerciseId());
				for (V1Blob.Set vs : nullSafe(ve.sets())) {
					entry.getSets().add(new WorkoutSet(
							vs.weight() == null ? 0 : vs.weight(),
							vs.reps() == null ? 0 : vs.reps(),
							Boolean.TRUE.equals(vs.done())));
				}
				w.getEntries().add(entry);
			}
			w.touch();
			workouts.save(w);
			workoutCount++;
		}

		return new ImportResult(true, "ok", workoutCount, exerciseCount);
	}

	private static <T> List<T> nullSafe(List<T> list) {
		return list == null ? List.of() : list;
	}
}
