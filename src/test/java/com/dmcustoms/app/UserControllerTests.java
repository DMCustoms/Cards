package com.dmcustoms.app;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.dmcustoms.app.data.dto.TransferDTO;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserControllerTests {

	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp(ApplicationContext applicationContext) {
		this.mockMvc = applicationContext.getBean(MockMvc.class);
	}

//	Get user cards tests

	@Test
	void test_showUserCards_unauthorized() throws Exception {
		this.mockMvc.perform(get("/api/user/cards")).andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("s.petrov@test.com")
	void test_showUserCards_authorized() throws Exception {
		this.mockMvc.perform(get("/api/user/cards")).andExpect(status().isOk());

		MvcResult result = mockMvc.perform(get("/api/user/cards")).andReturn();
		assertNotNull(result);
		assertEquals(result.getResponse().getContentType(), "application/json");
	}

//	Request to block card tests

	@Test
	void test_requestBlockCard_unauthorized() throws Exception {
		this.mockMvc.perform(patch("/api/user/block/2202202044507626").with(csrf())).andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("s.petrov@test.com")
	void test_requestBlockCard_userNotOwner() throws Exception {
		this.mockMvc.perform(patch("/api/user/block/2202202044507626").with(csrf())).andExpect(status().isBadRequest());
	}

	@Test
	@WithUserDetails("s.petrov@test.com")
	void test_blockCard_authorized_notFoundCard() throws Exception {
		this.mockMvc.perform(patch("/api/user/block/2653035107502819").with(csrf())).andExpect(status().isBadRequest());
	}

	@Test
	@WithUserDetails("o.solomatin@test.com")
	void test_blockCard_authorized_ok() throws Exception {
		this.mockMvc.perform(patch("/api/user/block/2202202044507626").with(csrf())).andExpect(status().isOk());
	}

//	Transfer between user cards tests

	@Test
	void test_transfer_unauthorized() throws Exception {
		this.mockMvc.perform(post("/api/user/transfer")).andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("s.petrov@test.com")
	void test_transfer_authorized_blankFields() throws Exception {
		TransferDTO object = new TransferDTO("", "", null);
		this.mockMvc
				.perform(post("/api/user/transfer").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithUserDetails("s.petrov@test.com")
	void test_transfer_authorized_notValidFields() throws Exception {
		TransferDTO object = new TransferDTO("1234123412341234", "1234123412341234", -1.);
		this.mockMvc
				.perform(post("/api/user/transfer").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
		MvcResult result = mockMvc
				.perform(post("/api/user/transfer").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andReturn();
		assertNotNull(result);
	}

}
