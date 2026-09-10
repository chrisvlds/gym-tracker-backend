package com.chrisvds.gymapi.exercise;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chrisvds.gymapi.common.NotFoundException;
import com.chrisvds.gymapi.identity.CurrentUser;
import com.chrisvds.gymapi.workout.WorkoutRepository;

@Service
public class ExerciseService {

	private final ExerciseRepository repo;
	private final WorkoutRepository workouts;
	private final CurrentUser currentUser;

	ExerciseService(ExerciseRepository repo, WorkoutRepository workouts, CurrentUser currentUser) {
		this.repo = repo;
		this.workouts = workouts;
		this.currentUser = currentUser;
	}

	/** Seeds the built-in catalog for a brand-new owner. Idempotent. */
	@Transactional
	public void ensureSeeded(String owner) {
		if (repo.existsByOwner(owner)) {
			return;
		}
		for (ExerciseCatalog.Entry entry : ExerciseCatalog.ENTRIES) {
			Exercise e = new Exercise(entry.id());
			e.setOwner(owner);
			e.setName(entry.name());
			e.setMuscle(entry.muscle());
			e.setEquipment(entry.equipment());
			e.setCustom(false);
			repo.save(e);
		}
	}

	@Transactional
	public List<ExerciseDto> list(boolean includeArchived) {
		String owner = currentUser.owner();
		ensureSeeded(owner);
		List<Exercise> found = includeArchived
				? repo.findByOwnerOrderByNameAsc(owner)
				: repo.findByOwnerAndArchivedFalseOrderByNameAsc(owner);
		return found.stream().map(ExerciseDto::of).toList();
	}

	@Transactional
	public ExerciseDto create(ExerciseRequest req) {
		if (req.name() == null || req.name().isBlank()) {
			throw new IllegalArgumentException("name is required");
		}
		String owner = currentUser.owner();
		ensureSeeded(owner);

		String id = (req.id() == null || req.id().isBlank())
				? "custom-" + UUID.randomUUID().toString().substring(0, 12)
				: req.id().trim();

		Exercise e = repo.findByIdAndOwner(id, owner).orElseGet(() -> {
			Exercise fresh = new Exercise(id);
			fresh.setOwner(owner);
			fresh.setCustom(true);
			return fresh;
		});
		e.setName(req.name().trim());
		e.setMuscle(req.muscle() == null || req.muscle().isBlank() ? "Other" : req.muscle().trim());
		e.setEquipment(req.equipment() == null ? Equipment.OTHER : req.equipment());
		e.setArchived(false);
		return ExerciseDto.of(repo.save(e));
	}

	@Transactional
	public ExerciseDto patch(String id, ExerciseRequest req) {
		Exercise e = mine(id);
		if (req.name() != null && !req.name().isBlank()) {
			e.setName(req.name().trim());
		}
		if (req.muscle() != null && !req.muscle().isBlank()) {
			e.setMuscle(req.muscle().trim());
		}
		if (req.equipment() != null) {
			e.setEquipment(req.equipment());
		}
		return ExerciseDto.of(repo.save(e));
	}

	/** Hard-delete if never used; otherwise archive so old workouts keep the name. */
	@Transactional
	public void delete(String id) {
		Exercise e = mine(id);
		if (workouts.existsByOwnerAndEntryExerciseId(e.getOwner(), id)) {
			e.setArchived(true);
			repo.save(e);
		} else {
			repo.delete(e);
		}
	}

	private Exercise mine(String id) {
		return repo.findByIdAndOwner(id, currentUser.owner())
				.orElseThrow(() -> new NotFoundException("exercise not found: " + id));
	}
}
