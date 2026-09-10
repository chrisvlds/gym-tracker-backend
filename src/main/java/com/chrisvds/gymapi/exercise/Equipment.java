package com.chrisvds.gymapi.exercise;

/**
 * Broad equipment class. Drives the default progression increment
 * (see {@link com.chrisvds.gymapi.progression.ProgressionService}) and is a
 * useful filter in the UI.
 */
public enum Equipment {

	MACHINE(5.0, 2.5),
	CABLE(5.0, 2.5),
	/** Small isolation movements where the stack jumps are best taken in half-steps. */
	CABLE_ISOLATION(2.5, 1.25),
	BARBELL(5.0, 2.5),
	DUMBBELL(5.0, 2.5),
	BODYWEIGHT(0.0, 0.0),
	OTHER(5.0, 2.5);

	private final double incrementLb;
	private final double incrementKg;

	Equipment(double incrementLb, double incrementKg) {
		this.incrementLb = incrementLb;
		this.incrementKg = incrementKg;
	}

	public double defaultIncrement(String unit) {
		return "kg".equalsIgnoreCase(unit) ? incrementKg : incrementLb;
	}
}
