package com.chrisvds.gymapi.workout;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import com.chrisvds.gymapi.support.MvcTest;
import com.jayway.jsonpath.JsonPath;

class WorkoutApiTest extends MvcTest {

	@Test
	void createReplaceEntriesAndRead() throws Exception {
		mvc.perform(post("/api/workouts").contentType("application/json").content("""
				{ "id": "w1", "date": "2026-09-01", "note": "felt good", "programDayKey": "push" }
				"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value("w1"))
				.andExpect(jsonPath("$.updatedAt").exists());

		mvc.perform(put("/api/workouts/w1/entries").contentType("application/json").content("""
				{ "entries": [
					{ "exerciseId": "chest-press-machine", "sets": [
						{ "weight": 100, "reps": 8, "done": true },
						{ "weight": 100, "reps": 8, "done": true }
					] }
				] }
				"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.entries[0].exerciseId").value("chest-press-machine"))
				.andExpect(jsonPath("$.entries[0].sets[1].reps").value(8));

		mvc.perform(get("/api/workouts/w1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.entries[0].sets.length()").value(2));

		mvc.perform(get("/api/workouts"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value("w1"))
				.andExpect(jsonPath("$[0].sets").value(2))
				.andExpect(jsonPath("$[0].volume").value(1600.0));
	}

	@Test
	void staleUpdateGets409() throws Exception {
		mvc.perform(post("/api/workouts").contentType("application/json")
				.content("{ \"id\": \"w2\", \"date\": \"2026-09-02\" }"))
				.andExpect(status().isCreated());

		mvc.perform(patch("/api/workouts/w2").contentType("application/json").content("""
				{ "note": "stale write", "expectedUpdatedAt": "2000-01-01T00:00:00Z" }
				"""))
				.andExpect(status().isConflict());
	}

	@Test
	void freshUpdateWithMatchingTimestampSucceeds() throws Exception {
		String bodyJson = mvc.perform(post("/api/workouts").contentType("application/json")
				.content("{ \"id\": \"w3\", \"date\": \"2026-09-03\" }"))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		String updatedAt = JsonPath.read(bodyJson, "$.updatedAt");

		mvc.perform(patch("/api/workouts/w3").contentType("application/json").content(
				"{ \"note\": \"ok\", \"expectedUpdatedAt\": \"" + updatedAt + "\" }"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.note").value("ok"));
	}

	@Test
	void deleteRemovesTheWorkout() throws Exception {
		mvc.perform(post("/api/workouts").contentType("application/json")
				.content("{ \"id\": \"w4\", \"date\": \"2026-09-04\" }"))
				.andExpect(status().isCreated());
		mvc.perform(delete("/api/workouts/w4")).andExpect(status().isNoContent());
		mvc.perform(get("/api/workouts/w4")).andExpect(status().isNotFound());
	}
}
