package com.chrisvds.gymapi.progression;

/** One performed set, decoupled from JPA so the logic stays trivially testable. */
public record SetPerf(double weight, int reps, boolean done) {
}
