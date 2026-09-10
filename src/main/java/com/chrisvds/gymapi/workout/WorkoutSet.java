package com.chrisvds.gymapi.workout;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "workout_set")
public class WorkoutSet {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private double weight;

	@Column(nullable = false)
	private int reps;

	@Column(nullable = false)
	private boolean done;

	protected WorkoutSet() {
	}

	public WorkoutSet(double weight, int reps, boolean done) {
		this.weight = weight;
		this.reps = reps;
		this.done = done;
	}

	public Long getId() {
		return id;
	}

	public double getWeight() {
		return weight;
	}

	public int getReps() {
		return reps;
	}

	public boolean isDone() {
		return done;
	}
}
