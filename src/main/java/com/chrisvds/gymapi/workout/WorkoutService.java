package com.chrisvds.gymapi.workout;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chrisvds.gymapi.common.ConflictException;
import com.chrisvds.gymapi.common.NotFoundException;
import com.chrisvds.gymapi.identity.CurrentUser;
import com.chrisvds.gymapi.workout.WorkoutDtos.CreateRequest;
import com.chrisvds.gymapi.workout.WorkoutDtos.EntriesRequest;
import com.chrisvds.gymapi.workout.WorkoutDtos.EntryDto;
import com.chrisvds.gymapi.workout.WorkoutDtos.PatchRequest;
import com.chrisvds.gymapi.workout.WorkoutDtos.WorkoutDto;
import com.chrisvds.gymapi.workout.WorkoutDtos.WorkoutSummaryDto;

@Service
public class WorkoutService {

	private final WorkoutRepository repo;
	private final CurrentUser currentUser;

	WorkoutService(WorkoutRepository repo, CurrentUser currentUser) {
		this.repo = repo;
		this.currentUser = currentUser;
	}

	@Transactional(readOnly = true)
	public List<WorkoutSummaryDto> list(LocalDate from, LocalDate to, Integer limit) {
		String owner = currentUser.owner();
		List<Workout> found = (from != null && to != null)
				? repo.findByOwnerAndDateBetweenOrderByDateDescCreatedAtDesc(owner, from, to)
				: repo.findByOwnerOrderByDateDescCreatedAtDesc(owner);
		if (limit != null && limit > 0 && found.size() > limit) {
			found = found.subList(0, limit);
		}
		return found.stream().map(WorkoutSummaryDto::of).toList();
	}

	@Transactional(readOnly = true)
	public WorkoutDto get(String id) {
		return WorkoutDto.of(mine(id));
	}

	/** Upsert by client-supplied id, so an offline-queued create is safe to retry. */
	@Transactional
	public WorkoutDto create(CreateRequest req) {
		if (req.id() == null || req.id().isBlank()) {
			throw new IllegalArgumentException("workout id is required");
		}
		String owner = currentUser.owner();
		Workout w = repo.findByIdAndOwner(req.id(), owner).orElseGet(() -> {
			Workout fresh = new Workout(req.id().trim());
			fresh.setOwner(owner);
			return fresh;
		});
		w.setDate(req.date() != null ? req.date() : LocalDate.now());
		w.setNote(req.note());
		w.setProgramDayKey(req.programDayKey());
		if (req.entries() != null) {
			applyEntries(w, req.entries());
		}
		w.touch();
		return WorkoutDto.of(repo.save(w));
	}

	@Transactional
	public WorkoutDto patch(String id, PatchRequest req) {
		Workout w = mine(id);
		checkConcurrency(w, req.expectedUpdatedAt());
		if (req.date() != null) {
			w.setDate(req.date());
		}
		if (req.note() != null) {
			w.setNote(req.note());
		}
		if (req.programDayKey() != null) {
			w.setProgramDayKey(req.programDayKey().isBlank() ? null : req.programDayKey());
		}
		w.touch();
		return WorkoutDto.of(repo.save(w));
	}

	/** Replace the whole entries+sets tree in one shot — the client's sync unit. */
	@Transactional
	public WorkoutDto replaceEntries(String id, EntriesRequest req) {
		Workout w = mine(id);
		checkConcurrency(w, req.expectedUpdatedAt());
		applyEntries(w, req.entries() == null ? List.of() : req.entries());
		w.touch();
		return WorkoutDto.of(repo.save(w));
	}

	@Transactional
	public void delete(String id) {
		repo.delete(mine(id));
	}

	private void applyEntries(Workout w, List<EntryDto> entries) {
		w.getEntries().clear();
		for (EntryDto ed : entries) {
			if (ed.exerciseId() == null || ed.exerciseId().isBlank()) {
				throw new IllegalArgumentException("entry.exerciseId is required");
			}
			WorkoutEntry entry = new WorkoutEntry(ed.exerciseId().trim());
			if (ed.sets() != null) {
				ed.sets().forEach(s -> entry.getSets().add(new WorkoutSet(
						Math.max(0, s.weight()), Math.max(0, s.reps()), s.done())));
			}
			w.getEntries().add(entry);
		}
	}

	private void checkConcurrency(Workout w, Instant expected) {
		if (expected != null && !expected.equals(w.getUpdatedAt())) {
			throw new ConflictException(
					"workout changed on another device (server updatedAt=" + w.getUpdatedAt() + ")");
		}
	}

	private Workout mine(String id) {
		return repo.findByIdAndOwner(id, currentUser.owner())
				.orElseThrow(() -> new NotFoundException("workout not found: " + id));
	}
}
