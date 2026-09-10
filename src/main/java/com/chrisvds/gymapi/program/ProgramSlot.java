package com.chrisvds.gymapi.program;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** One planned exercise on a program day, with its target and progression step. */
@Entity
@Table(name = "program_slot")
public class ProgramSlot {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 64)
	private String exerciseId;

	@Column(nullable = false)
	private int targetSets = 3;

	@Column(nullable = false)
	private int repMin = 6;

	@Column(nullable = false)
	private int repMax = 8;

	/** Override for the per-session weight jump; null → derive from the exercise's equipment. */
	private Double increment;

	protected ProgramSlot() {
	}

	public ProgramSlot(String exerciseId) {
		this.exerciseId = exerciseId;
	}

	public Long getId() {
		return id;
	}

	public String getExerciseId() {
		return exerciseId;
	}

	public int getTargetSets() {
		return targetSets;
	}

	public void setTargetSets(int targetSets) {
		this.targetSets = targetSets;
	}

	public int getRepMin() {
		return repMin;
	}

	public void setRepMin(int repMin) {
		this.repMin = repMin;
	}

	public int getRepMax() {
		return repMax;
	}

	public void setRepMax(int repMax) {
		this.repMax = repMax;
	}

	public Double getIncrement() {
		return increment;
	}

	public void setIncrement(Double increment) {
		this.increment = increment;
	}
}
