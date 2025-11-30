package com.dmcustoms.app;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminControllerTests {

	MockMvc mockMvc;

	@BeforeEach
	void setUp(ApplicationContext applicationContext) {
		this.mockMvc = applicationContext.getBean(MockMvc.class);
	}

	@Test
	void test_showUsers_unauthorized() throws Exception {
		this.mockMvc.perform(get("/api/admin/users")).andExpect(status().isUnauthorized());
	}
	
	@Test
	@WithUserDetails("s.petrov@test.com")
	void test_showUsers_notAdmin() throws Exception {
		this.mockMvc.perform(get("/api/admin/users")).andExpect(status().isForbidden());
	}
	
	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_showUsers_authorized() throws Exception {
		this.mockMvc.perform(get("/api/admin/users")).andExpect(status().isOk());
		
		MvcResult result = mockMvc.perform(get("/api/admin/users")).andReturn();
		assertNotNull(result);
		assertEquals(result.getResponse().getContentType(), "application/json");
	}

}
