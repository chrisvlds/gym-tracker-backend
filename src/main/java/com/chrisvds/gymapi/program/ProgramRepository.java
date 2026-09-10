package com.chrisvds.gymapi.program;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramRepository extends JpaRepository<Program, String> {

	Optional<Program> findByOwner(String owner);
}
