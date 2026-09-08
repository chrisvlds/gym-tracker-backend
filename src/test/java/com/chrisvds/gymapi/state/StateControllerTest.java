package com.chrisvds.gymapi.state;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
@Transactional
class StateControllerTest {

	@Autowired
	WebApplicationContext context;

	MockMvc mvc;

	@BeforeEach
	void setUp() {
		mvc = webAppContextSetup(context).build();
	}

	@Test
	void getReturns204WhenEmpty() throws Exception {
		mvc.perform(get("/api/state")).andExpect(status().isNoContent());
	}

	@Test
	void putThenGetRoundTrips() throws Exception {
		String body = "{\"version\":1,\"workouts\":[{\"id\":\"a\"}]}";

		mvc.perform(put("/api/state").contentType("application/json").content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.updatedAt").exists());

		mvc.perform(get("/api/state"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.payload.version").value(1))
				.andExpect(jsonPath("$.payload.workouts[0].id").value("a"))
				.andExpect(jsonPath("$.updatedAt").exists());
	}
}
