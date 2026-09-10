package com.chrisvds.gymapi.exercise;

import java.util.List;

/**
 * The built-in exercise catalog, seeded once per owner. Stable ids so the
 * program's default slots (see {@link com.chrisvds.gymapi.program.ProgramDefaults})
 * can reference them directly.
 */
public final class ExerciseCatalog {

	public record Entry(String id, String name, String muscle, Equipment equipment) {
	}

	private ExerciseCatalog() {
	}

	public static final List<Entry> ENTRIES = List.of(
			// --- The user's PPL movements (stable ids used by ProgramDefaults) ---
			new Entry("chest-press-machine", "Chest Press Machine", "Chest", Equipment.MACHINE),
			new Entry("lateral-raise-cable", "Cable Lateral Raise", "Shoulders", Equipment.CABLE_ISOLATION),
			new Entry("chest-fly-machine", "Chest Fly Machine", "Chest", Equipment.MACHINE),
			new Entry("tricep-pushdown-cable", "Cable Tricep Pushdown", "Triceps", Equipment.CABLE_ISOLATION),
			new Entry("lat-pulldown-cable", "Cable Lat Pulldown", "Back", Equipment.CABLE),
			new Entry("chest-supported-row", "Chest-Supported Row", "Back", Equipment.MACHINE),
			new Entry("single-arm-lat-pulldown", "Single-Arm Lat Pulldown", "Back", Equipment.CABLE),
			new Entry("preacher-curl", "Preacher Curl", "Biceps", Equipment.MACHINE),
			new Entry("hammer-curl", "Hammer Curl", "Biceps", Equipment.DUMBBELL),
			new Entry("hack-squat", "Hack Squat", "Quads", Equipment.MACHINE),
			new Entry("leg-press", "Leg Press", "Quads", Equipment.MACHINE),
			new Entry("leg-extension", "Leg Extension", "Quads", Equipment.MACHINE),
			new Entry("walking-lunge", "Walking Lunge", "Quads", Equipment.DUMBBELL),
			new Entry("standing-calf-raise", "Standing Calf Raise", "Calves", Equipment.MACHINE),
			new Entry("romanian-deadlift", "Romanian Deadlift", "Hamstrings", Equipment.BARBELL),
			new Entry("seated-leg-curl", "Seated Leg Curl", "Hamstrings", Equipment.MACHINE),
			new Entry("hip-thrust", "Hip Thrust", "Glutes", Equipment.BARBELL),
			new Entry("back-extension", "Back Extension", "Glutes", Equipment.BODYWEIGHT),
			new Entry("seated-calf-raise", "Seated Calf Raise", "Calves", Equipment.MACHINE),

			// --- General catalog (carried over from the old frontend defaults) ---
			new Entry("bench-press", "Barbell Bench Press", "Chest", Equipment.BARBELL),
			new Entry("incline-db-press", "Incline Dumbbell Press", "Chest", Equipment.DUMBBELL),
			new Entry("push-up", "Push-up", "Chest", Equipment.BODYWEIGHT),
			new Entry("cable-chest-fly", "Cable Chest Fly", "Chest", Equipment.CABLE),
			new Entry("deadlift", "Deadlift", "Back", Equipment.BARBELL),
			new Entry("barbell-row", "Barbell Row", "Back", Equipment.BARBELL),
			new Entry("pull-up", "Pull-up", "Back", Equipment.BODYWEIGHT),
			new Entry("lat-pulldown", "Lat Pulldown", "Back", Equipment.MACHINE),
			new Entry("seated-row", "Seated Cable Row", "Back", Equipment.CABLE),
			new Entry("ohp", "Overhead Press", "Shoulders", Equipment.BARBELL),
			new Entry("db-shoulder-press", "Dumbbell Shoulder Press", "Shoulders", Equipment.DUMBBELL),
			new Entry("lateral-raise", "Lateral Raise", "Shoulders", Equipment.DUMBBELL),
			new Entry("face-pull", "Face Pull", "Shoulders", Equipment.CABLE),
			new Entry("barbell-curl", "Barbell Curl", "Biceps", Equipment.BARBELL),
			new Entry("db-curl", "Dumbbell Curl", "Biceps", Equipment.DUMBBELL),
			new Entry("tricep-pushdown", "Tricep Pushdown", "Triceps", Equipment.CABLE),
			new Entry("skull-crusher", "Skull Crusher", "Triceps", Equipment.BARBELL),
			new Entry("dip", "Dip", "Triceps", Equipment.BODYWEIGHT),
			new Entry("back-squat", "Barbell Back Squat", "Quads", Equipment.BARBELL),
			new Entry("front-squat", "Front Squat", "Quads", Equipment.BARBELL),
			new Entry("lunge", "Walking Lunge (Bodyweight)", "Quads", Equipment.BODYWEIGHT),
			new Entry("rdl", "Romanian Deadlift (Dumbbell)", "Hamstrings", Equipment.DUMBBELL),
			new Entry("leg-curl", "Lying Leg Curl", "Hamstrings", Equipment.MACHINE),
			new Entry("calf-raise", "Standing Calf Raise (Bodyweight)", "Calves", Equipment.BODYWEIGHT),
			new Entry("plank", "Plank", "Core", Equipment.BODYWEIGHT),
			new Entry("hanging-leg-raise", "Hanging Leg Raise", "Core", Equipment.BODYWEIGHT),
			new Entry("cable-crunch", "Cable Crunch", "Core", Equipment.CABLE));
}
