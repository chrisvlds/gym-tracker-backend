package com.chrisvds.gymapi.progression;

/**
 * Advice for the next time an exercise comes up.
 *
 * @param action  one of {@code increase}, {@code add-reps}, {@code hold},
 *                {@code hold-or-deload}, {@code first-time}
 * @param weight  the weight to work with next session (null when unknown)
 * @param text    a short sentence for the UI
 */
public record Suggestion(String action, Double weight, String text) {

	public static final String INCREASE = "increase";
	public static final String ADD_REPS = "add-reps";
	public static final String HOLD = "hold";
	public static final String HOLD_OR_DELOAD = "hold-or-deload";
	public static final String FIRST_TIME = "first-time";
}
