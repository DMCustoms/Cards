package com.dmcustoms.app;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import com.dmcustoms.app.data.dto.CardCreateDTO;
import com.dmcustoms.app.data.dto.UserCreateDTO;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AdminControllerTests {

	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp(ApplicationContext applicationContext) {
		this.mockMvc = applicationContext.getBean(MockMvc.class);
	}

//	Creation cards tests

	@Test
	void test_createCard_unauthorized() throws Exception {
		this.mockMvc.perform(post("/api/admin/create/card").with(csrf())).andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("s.petrov@test.com")
	void test_createCard_notAdmin() throws Exception {
		this.mockMvc.perform(post("/api/admin/create/card").with(csrf())).andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_createCard_authorized_nullValues() throws Exception {
		CardCreateDTO object = new CardCreateDTO("", null, null, null);
		this.mockMvc
				.perform(post("/api/admin/create/card").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_createCard_authorized_notValidCardNumber() throws Exception {
		CardCreateDTO object = new CardCreateDTO("1234123412341234", 0., 10000000.00, 10000000.00);
		this.mockMvc
				.perform(post("/api/admin/create/card").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_createCard_authorized_notUniqueCardNumber() throws Exception {
		CardCreateDTO object = new CardCreateDTO("2202202044507626", 0., 10000000.00, 10000000.00);
		this.mockMvc
				.perform(post("/api/admin/create/card").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_createCard_authorized_created() throws Exception {
		CardCreateDTO object = new CardCreateDTO("2200170202743022", 0., 10000000.00, 10000000.00);
		this.mockMvc
				.perform(post("/api/admin/create/card").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated());
	}

//	Block cards tests

	@Test
	void test_blockCard_unauthorized() throws Exception {
		this.mockMvc.perform(post("/api/admin/cards/block/2202202044507626").with(csrf()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("s.petrov@test.com")
	void test_blockCard_notAdmin() throws Exception {
		this.mockMvc.perform(post("/api/admin/cards/block/2202202044507626").with(csrf()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_blockCard_authorized_notFoundCard() throws Exception {
		this.mockMvc.perform(post("/api/admin/cards/block/2202202044507000").with(csrf()))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_blockCard_authorized_blocked() throws Exception {
		this.mockMvc.perform(post("/api/admin/cards/block/2202202044507626").with(csrf())).andExpect(status().isOk());
	}

//	Activate cards tests

	@Test
	void test_activateCard_unauthorized() throws Exception {
		this.mockMvc.perform(post("/api/admin/cards/activate/2202202044507626").with(csrf()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("s.petrov@test.com")
	void test_activateCard_notAdmin() throws Exception {
		this.mockMvc.perform(post("/api/admin/cards/activate/2202202044507626").with(csrf()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_activateCard_authorized_notFoundCard() throws Exception {
		this.mockMvc.perform(post("/api/admin/cards/activate/2202202044507000").with(csrf()))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_activateCard_authorized_activated() throws Exception {
		this.mockMvc.perform(post("/api/admin/cards/activate/2202202044507626").with(csrf()))
				.andExpect(status().isOk());
	}
	
//	Delete cards tests
	
	@Test
	void test_deleteCard_unauthorized() throws Exception {
		this.mockMvc.perform(delete("/api/admin/cards/2202202044507626").with(csrf()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("s.petrov@test.com")
	void test_deleteCard_notAdmin() throws Exception {
		this.mockMvc.perform(delete("/api/admin/cards/2202202044507626").with(csrf()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_deleteCard_authorized_notFoundCard() throws Exception {
		this.mockMvc.perform(delete("/api/admin/cards/2202202044507000").with(csrf()))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_deleteCard_authorized_deleted() throws Exception {
		this.mockMvc.perform(delete("/api/admin/cards/2202202044507626").with(csrf()))
				.andExpect(status().isNoContent());
	}

//	Creation users tests

	@Test
	void test_createUser_unauthorized() throws Exception {
		this.mockMvc.perform(post("/api/admin/create/user")).andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("s.petrov@test.com")
	void test_createUser_notAdmin() throws Exception {
		this.mockMvc.perform(post("/api/admin/create/user")).andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_createUser_authorized_blankFields() throws Exception {
		UserCreateDTO object = new UserCreateDTO("", "", "", "", "");
		this.mockMvc
				.perform(post("/api/admin/create/user").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_createUser_authorized_notValidEmail() throws Exception {
		UserCreateDTO object = new UserCreateDTO("Ivanov", "Ivan", "Ivanovich", "i.ivanov", "password");
		this.mockMvc
				.perform(post("/api/admin/create/user").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_createUser_authorized_notUniqueEmail() throws Exception {
		UserCreateDTO object = new UserCreateDTO("Ivanov", "Ivan", "Ivanovich", "i.ivanov@test.com", "password");
		this.mockMvc
				.perform(post("/api/admin/create/user").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_createUser_authorized_created() throws Exception {
		UserCreateDTO object = new UserCreateDTO("Ivanov", "Ivan", "Ivanovich", "i.i.ivanov@test.com", "password");
		this.mockMvc
				.perform(post("/api/admin/create/user").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated());
	}

//	Show users tests

	@Test
	void test_showUsers_unauthorized() throws Exception {
		this.mockMvc.perform(get("/api/admin/users")).andExpect(status().isForbidden());
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
