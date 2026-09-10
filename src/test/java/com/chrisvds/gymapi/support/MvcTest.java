package com.chrisvds.gymapi.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * Base for controller integration tests. Boot 4 doesn't ship
 * {@code @AutoConfigureMockMvc} on this classpath, so we build {@link MockMvc}
 * from the context the same way the original {@code StateControllerTest} did.
 * {@code @Transactional} rolls back the per-owner seed data between tests.
 */
@SpringBootTest
@Transactional
public abstract class MvcTest {

	@Autowired
	private WebApplicationContext context;

	protected MockMvc mvc;

	@BeforeEach
	void setUpMockMvc() {
		mvc = MockMvcBuilders.webAppContextSetup(context).build();
	}
}
