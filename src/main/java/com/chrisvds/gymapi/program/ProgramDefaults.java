package com.chrisvds.gymapi.program;

import java.util.List;
import java.util.Map;

/**
 * The default 6-day PPL program seeded for a new owner:
 * {@code Push → Pull → Legs (Quad) → Push → Pull → Legs (Ham/Glute)}, every
 * exercise 3 sets of 6–8. All editable afterwards via {@code PUT /api/program}.
 */
final class ProgramDefaults {

	private ProgramDefaults() {
	}

	static final List<String> CYCLE = List.of(
			"push", "pull", "legs-quad", "push", "pull", "legs-ham");

	private static final Map<String, String> DAY_NAMES = Map.of(
			"push", "Push",
			"pull", "Pull",
			"legs-quad", "Legs — Quad focus",
			"legs-ham", "Legs — Ham & Glute focus");

	private static final Map<String, List<String>> DAY_EXERCISES = Map.of(
			"push", List.of(
					"chest-press-machine", "lateral-raise-cable", "chest-fly-machine", "tricep-pushdown-cable"),
			"pull", List.of(
					"lat-pulldown-cable", "chest-supported-row", "single-arm-lat-pulldown",
					"preacher-curl", "hammer-curl"),
			"legs-quad", List.of(
					"hack-squat", "leg-press", "leg-extension", "walking-lunge", "standing-calf-raise"),
			"legs-ham", List.of(
					"romanian-deadlift", "seated-leg-curl", "hip-thrust", "back-extension", "seated-calf-raise"));

	/** Distinct day keys, in a stable order for the editor. */
	static final List<String> DAY_KEYS = List.of("push", "pull", "legs-quad", "legs-ham");

	static Program build(String id, String owner) {
		Program program = new Program(id);
		program.setOwner(owner);
		program.setName("PPL");
		program.getCycle().addAll(CYCLE);

		for (String key : DAY_KEYS) {
			ProgramDay day = new ProgramDay(key, DAY_NAMES.get(key));
			for (String exerciseId : DAY_EXERCISES.get(key)) {
				day.getSlots().add(new ProgramSlot(exerciseId)); // 3 x 6–8, increment from equipment
			}
			program.getDays().add(day);
		}
		return program;
	}
}
