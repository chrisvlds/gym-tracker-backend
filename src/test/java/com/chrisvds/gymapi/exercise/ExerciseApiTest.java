package com.chrisvds.gymapi.exercise;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import com.chrisvds.gymapi.support.MvcTest;

class ExerciseApiTest extends MvcTest {

	@Test
	void catalogIsSeededOnFirstRead() throws Exception {
		mvc.perform(get("/api/exercises"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.id == 'chest-press-machine')].name").value("Chest Press Machine"))
				.andExpect(jsonPath("$[?(@.id == 'hammer-curl')].equipment").value("DUMBBELL"));
	}

	@Test
	void createCustomExerciseAndArchiveWhenReferenced() throws Exception {
		mvc.perform(post("/api/exercises").contentType("application/json").content("""
				{ "id": "cust-1", "name": "Cable Y-Raise", "muscle": "Shoulders", "equipment": "CABLE_ISOLATION" }
				"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.custom").value(true));

		// reference it in a workout, then delete -> should archive, not vanish
		mvc.perform(post("/api/workouts").contentType("application/json")
				.content("{ \"id\": \"wx\", \"date\": \"2026-09-01\" }"))
				.andExpect(status().isCreated());
		mvc.perform(put("/api/workouts/wx/entries").contentType("application/json").content(
				"{ \"entries\": [ { \"exerciseId\": \"cust-1\", \"sets\": [ { \"weight\": 10, \"reps\": 12, \"done\": true } ] } ] }"))
				.andExpect(status().isOk());

		mvc.perform(delete("/api/exercises/cust-1")).andExpect(status().isNoContent());

		mvc.perform(get("/api/exercises?includeArchived=true"))
				.andExpect(jsonPath("$[?(@.id == 'cust-1')].archived").value(true));
	}
}
