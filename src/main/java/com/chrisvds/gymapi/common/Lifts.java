package com.chrisvds.gymapi.common;

/** Small strength-math helpers, matching the formulas the frontend used. */
public final class Lifts {

	private Lifts() {
	}

	/** Epley estimated one-rep max. */
	public static double estimate1RM(double weight, int reps) {
		if (weight <= 0 || reps <= 0) {
			return 0;
		}
		if (reps == 1) {
			return weight;
		}
		return weight * (1 + reps / 30.0);
	}

	public static double setVolume(double weight, int reps) {
		return Math.max(0, weight) * Math.max(0, reps);
	}

	public static double round(double n, int places) {
		double f = Math.pow(10, places);
		return Math.round(n * f) / f;
	}
}
