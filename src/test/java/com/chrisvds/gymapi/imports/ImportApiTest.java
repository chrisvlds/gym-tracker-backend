package com.chrisvds.gymapi.imports;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import com.chrisvds.gymapi.support.MvcTest;

class ImportApiTest extends MvcTest {

	private static final String V1 = """
			{
			  "version": 1,
			  "settings": { "unit": "kg" },
			  "customExercises": [
			    { "id": "custom-abc", "name": "Landmine Press", "muscle": "Shoulders", "custom": true }
			  ],
			  "workouts": [
			    {
			      "id": "old-1", "date": "2026-08-20", "note": "carried over",
			      "entries": [
			        { "id": "e1", "exerciseId": "chest-press-machine", "sets": [
			          { "id": "s1", "weight": 90, "reps": 8, "done": true },
			          { "id": "s2", "weight": 90, "reps": 7, "done": true }
			        ] },
			        { "id": "e2", "exerciseId": "custom-abc", "sets": [
			          { "id": "s3", "weight": 40, "reps": 10, "done": true }
			        ] }
			      ]
			    }
			  ]
			}
			""";

	@Test
	void importsV1BlobIntoRows() throws Exception {
		mvc.perform(post("/api/import").contentType("application/json").content(V1))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.imported").value(true))
				.andExpect(jsonPath("$.workouts").value(1))
				.andExpect(jsonPath("$.exercises").value(1));

		mvc.perform(get("/api/workouts/old-1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.note").value("carried over"))
				.andExpect(jsonPath("$.entries.length()").value(2))
				.andExpect(jsonPath("$.entries[0].sets[1].reps").value(7));

		mvc.perform(get("/api/exercises"))
				.andExpect(jsonPath("$[?(@.id == 'custom-abc')].name").value("Landmine Press"));

		mvc.perform(get("/api/settings"))
				.andExpect(jsonPath("$.unit").value("kg"));
	}

	@Test
	void secondImportIsRefusedWithoutForce() throws Exception {
		mvc.perform(post("/api/import").contentType("application/json").content(V1))
				.andExpect(status().isOk());
		mvc.perform(post("/api/import").contentType("application/json").content(V1))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.imported").value(false));
		mvc.perform(post("/api/import?force=true").contentType("application/json").content(V1))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.imported").value(true));
	}
}
