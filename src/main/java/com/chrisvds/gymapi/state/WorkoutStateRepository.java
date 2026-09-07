package com.chrisvds.gymapi.state;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutStateRepository extends JpaRepository<WorkoutState, String> {
}
