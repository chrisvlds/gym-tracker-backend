package com.chrisvds.gymapi.program;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import com.chrisvds.gymapi.support.MvcTest;

class ProgramApiTest extends MvcTest {

	@Test
	void programIsSeededWithThePplDefault() throws Exception {
		mvc.perform(get("/api/program"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("PPL"))
				.andExpect(jsonPath("$.cycle.length()").value(6))
				.andExpect(jsonPath("$.cycle[0]").value("push"))
				.andExpect(jsonPath("$.cycle[2]").value("legs-quad"))
				.andExpect(jsonPath("$.days.length()").value(4));
	}

	@Test
	void nextStartsAtPushWithNoHistory() throws Exception {
		mvc.perform(get("/api/program/next"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.dayKey").value("push"))
				.andExpect(jsonPath("$.exercises[0].exerciseId").value("chest-press-machine"))
				.andExpect(jsonPath("$.exercises[0].targetSets").value(3))
				.andExpect(jsonPath("$.exercises[0].suggestion.action").value("first-time"));
	}

	@Test
	void nextAdvancesToPullAfterAPushWorkout() throws Exception {
		logProgramWorkout("p1", "2026-09-01", "push", "chest-press-machine", 10);

		mvc.perform(get("/api/program/next"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.dayKey").value("pull"))
				.andExpect(jsonPath("$.lastDayKey").value("push"));
	}

	@Test
	void progressionSuggestsMoreWeightAfterHittingTheTopOfTheRange() throws Exception {
		// Full cycle back to push; the push session hit 10s, so next push says "go up".
		logProgramWorkout("p1", "2026-09-01", "push", "chest-press-machine", 10);
		logProgramWorkout("p2", "2026-09-03", "pull", "lat-pulldown-cable", 8);
		logProgramWorkout("p3", "2026-09-05", "legs-quad", "hack-squat", 8);

		mvc.perform(get("/api/program/next"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.dayKey").value("push"))
				.andExpect(jsonPath("$.exercises[0].exerciseId").value("chest-press-machine"))
				.andExpect(jsonPath("$.exercises[0].suggestion.action").value("increase"))
				.andExpect(jsonPath("$.exercises[0].suggestion.weight").value(105.0))
				.andExpect(jsonPath("$.exercises[0].lastPerformance[0].reps").value(10));
	}

	@Test
	void editingTheProgramPersists() throws Exception {
		mvc.perform(put("/api/program").contentType("application/json").content("""
				{ "name": "PPL v2", "cycle": ["push", "pull"], "days": [
					{ "key": "push", "name": "Push", "slots": [
						{ "exerciseId": "chest-press-machine", "targetSets": 4, "repMin": 5, "repMax": 8 }
					] },
					{ "key": "pull", "name": "Pull", "slots": [
						{ "exerciseId": "lat-pulldown-cable", "targetSets": 3, "repMin": 8, "repMax": 12 }
					] }
				] }
				"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("PPL v2"))
				.andExpect(jsonPath("$.cycle.length()").value(2))
				.andExpect(jsonPath("$.days[0].slots[0].targetSets").value(4));

		mvc.perform(get("/api/program"))
				.andExpect(jsonPath("$.days[0].slots[0].repMin").value(5));
	}

	private void logProgramWorkout(String id, String date, String dayKey, String exerciseId, int reps)
			throws Exception {
		mvc.perform(post("/api/workouts").contentType("application/json").content(
				"{ \"id\": \"" + id + "\", \"date\": \"" + date + "\", \"programDayKey\": \"" + dayKey + "\" }"))
				.andExpect(status().isCreated());
		mvc.perform(put("/api/workouts/" + id + "/entries").contentType("application/json").content("""
				{ "entries": [ { "exerciseId": "%s", "sets": [
					{ "weight": 100, "reps": %d, "done": true },
					{ "weight": 100, "reps": %d, "done": true },
					{ "weight": 100, "reps": %d, "done": true }
				] } ] }
				""".formatted(exerciseId, reps, reps, reps)))
				.andExpect(status().isOk());
	}
}
