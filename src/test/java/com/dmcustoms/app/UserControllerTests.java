package com.dmcustoms.app;

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTests {
	
	MockMvc mockMvc;
	
	@BeforeEach
	void setUp(ApplicationContext applicationContext) {
		this.mockMvc = applicationContext.getBean(MockMvc.class);
	}

	@Test
	void test_showUserCards_unauthorized() throws Exception {
		this.mockMvc.perform(get("/api/user/cards")).andExpect(status().isUnauthorized());
	}
	
	@Test
	@WithMockUser(username = "wrong.user@test.com")
	void test_showUserCards_wrongUser() throws Exception {
		this.mockMvc.perform(get("/api/user/cards")).andExpect(status().isNoContent());
	}
	
	@Test
	@WithUserDetails("s.petrov@test.com")
	void test_showUserCards_authorized() throws Exception {
		this.mockMvc.perform(get("/api/user/cards")).andExpect(status().isOk());
		
		MvcResult result = mockMvc.perform(get("/api/user/cards")).andReturn();
		assertNotNull(result);
		assertEquals(result.getResponse().getContentType(), "application/json");
	}
	
}
