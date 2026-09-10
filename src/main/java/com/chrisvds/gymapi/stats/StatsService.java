package com.chrisvds.gymapi.stats;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chrisvds.gymapi.common.Lifts;
import com.chrisvds.gymapi.exercise.Exercise;
import com.chrisvds.gymapi.exercise.ExerciseRepository;
import com.chrisvds.gymapi.identity.CurrentUser;
import com.chrisvds.gymapi.stats.StatsDtos.ExerciseHistory;
import com.chrisvds.gymapi.stats.StatsDtos.PersonalRecord;
import com.chrisvds.gymapi.stats.StatsDtos.Session;
import com.chrisvds.gymapi.stats.StatsDtos.SetView;
import com.chrisvds.gymapi.workout.Workout;
import com.chrisvds.gymapi.workout.WorkoutEntry;
import com.chrisvds.gymapi.workout.WorkoutRepository;
import com.chrisvds.gymapi.workout.WorkoutSet;

@Service
public class StatsService {

	private final WorkoutRepository workouts;
	private final ExerciseRepository exercises;
	private final CurrentUser currentUser;

	StatsService(WorkoutRepository workouts, ExerciseRepository exercises, CurrentUser currentUser) {
		this.workouts = workouts;
		this.exercises = exercises;
		this.currentUser = currentUser;
	}

	@Transactional(readOnly = true)
	public ExerciseHistory exerciseHistory(String exerciseId) {
		String owner = currentUser.owner();
		String name = exercises.findByIdAndOwner(exerciseId, owner)
				.map(Exercise::getName).orElse(exerciseId);

		List<Session> sessions = new ArrayList<>();
		PersonalRecord pr = null;

		for (Workout w : workouts.findHistoryForExercise(owner, exerciseId)) {
			WorkoutEntry entry = w.getEntries().stream()
					.filter(e -> e.getExerciseId().equals(exerciseId))
					.findFirst().orElse(null);
			if (entry == null) {
				continue;
			}

			List<SetView> setViews = new ArrayList<>();
			double volume = 0;
			double bestWeight = 0;
			int bestReps = 0;
			double best1RM = 0;
			for (WorkoutSet s : entry.getSets()) {
				setViews.add(new SetView(s.getWeight(), s.getReps(), s.isDone()));
				volume += Lifts.setVolume(s.getWeight(), s.getReps());
				double e1 = Lifts.estimate1RM(s.getWeight(), s.getReps());
				if (e1 > best1RM) {
					best1RM = e1;
					bestWeight = s.getWeight();
					bestReps = s.getReps();
				}
			}

			sessions.add(new Session(w.getDate(), w.getId(), w.getProgramDayKey(), setViews,
					Lifts.round(bestWeight, 2), bestReps, Lifts.round(best1RM, 1), Lifts.round(volume, 1)));

			if (best1RM > 0 && (pr == null || best1RM > pr.e1RM())) {
				pr = new PersonalRecord(w.getDate(), w.getId(),
						Lifts.round(bestWeight, 2), bestReps, Lifts.round(best1RM, 1));
			}
		}

		return new ExerciseHistory(exerciseId, name, sessions.size(), pr, sessions);
	}
}
