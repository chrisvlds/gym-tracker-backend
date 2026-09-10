package com.chrisvds.gymapi.workout;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkoutRepository extends JpaRepository<Workout, String> {

	List<Workout> findByOwnerOrderByDateDescCreatedAtDesc(String owner);

	List<Workout> findByOwnerAndDateBetweenOrderByDateDescCreatedAtDesc(
			String owner, LocalDate from, LocalDate to);

	Optional<Workout> findByIdAndOwner(String id, String owner);

	Optional<Workout> findFirstByOwnerAndDateOrderByCreatedAtDesc(String owner, LocalDate date);

	Optional<Workout> findFirstByOwnerAndProgramDayKeyIsNotNullOrderByDateDescCreatedAtDesc(String owner);

	@Query("""
			select case when count(w) > 0 then true else false end
			from Workout w join w.entries e
			where w.owner = :owner and e.exerciseId = :exerciseId
			""")
	boolean existsByOwnerAndEntryExerciseId(
			@Param("owner") String owner, @Param("exerciseId") String exerciseId);

	@Query("""
			select distinct w from Workout w join w.entries e
			where w.owner = :owner and e.exerciseId = :exerciseId
			order by w.date asc, w.createdAt asc
			""")
	List<Workout> findHistoryForExercise(
			@Param("owner") String owner, @Param("exerciseId") String exerciseId);
}
