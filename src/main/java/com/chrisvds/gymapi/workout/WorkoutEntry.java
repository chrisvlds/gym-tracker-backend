package com.chrisvds.gymapi.workout;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

/** One exercise within a workout, holding its ordered sets. */
@Entity
@Table(name = "workout_entry")
public class WorkoutEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 64)
	private String exerciseId;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "entry_id")
	@OrderColumn(name = "position")
	private List<WorkoutSet> sets = new ArrayList<>();

	protected WorkoutEntry() {
	}

	public WorkoutEntry(String exerciseId) {
		this.exerciseId = exerciseId;
	}

	public Long getId() {
		return id;
	}

	public String getExerciseId() {
		return exerciseId;
	}

	public List<WorkoutSet> getSets() {
		return sets;
	}
}
