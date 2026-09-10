package com.chrisvds.gymapi.exercise;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseRepository extends JpaRepository<Exercise, String> {

	List<Exercise> findByOwnerOrderByNameAsc(String owner);

	List<Exercise> findByOwnerAndArchivedFalseOrderByNameAsc(String owner);

	Optional<Exercise> findByIdAndOwner(String id, String owner);

	boolean existsByOwner(String owner);
}
