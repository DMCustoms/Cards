package com.dmcustoms.app;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

import com.dmcustoms.app.data.dto.AddCardToUserDTO;
import com.dmcustoms.app.data.dto.CardCreateDTO;
import com.dmcustoms.app.data.dto.SetLimitsDTO;
import com.dmcustoms.app.data.dto.UserCreateDTO;
import com.dmcustoms.app.data.repositories.CardRepository;
import com.dmcustoms.app.data.entities.Card;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AdminControllerTests {

	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private CardRepository cardRepository;

	@BeforeEach
	void setUp(ApplicationContext applicationContext) {
		this.mockMvc = applicationContext.getBean(MockMvc.class);
	}

//	Creation cards tests

	@Test
	void test_createCard_unauthorized() throws Exception {
		this.mockMvc.perform(post("/api/admin/cards/create").with(csrf())).andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("s.petrov@test.com")
	void test_createCard_notAdmin() throws Exception {
		this.mockMvc.perform(post("/api/admin/cards/create").with(csrf())).andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_createCard_authorized_nullValues() throws Exception {
		CardCreateDTO object = new CardCreateDTO("", null, null, null);
		this.mockMvc
				.perform(post("/api/admin/cards/create").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_createCard_authorized_notValidCardNumber() throws Exception {
		CardCreateDTO object = new CardCreateDTO("1234123412341234", 0., 10000000.00, 10000000.00);
		this.mockMvc
				.perform(post("/api/admin/cards/create").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_createCard_authorized_notUniqueCardNumber() throws Exception {
		CardCreateDTO object = new CardCreateDTO("2202202044507626", 0., 10000000.00, 10000000.00);
		this.mockMvc
				.perform(post("/api/admin/cards/create").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isConflict());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_createCard_authorized_created() throws Exception {
		CardCreateDTO object = new CardCreateDTO("2200170202743022", 0., 10000000.00, 10000000.00);
		this.mockMvc
				.perform(post("/api/admin/cards/create").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated());
	}

//	Block cards tests

	@Test
	void test_blockCard_unauthorized() throws Exception {
		this.mockMvc.perform(patch("/api/admin/cards/block/2202202044507626").with(csrf()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("s.petrov@test.com")
	void test_blockCard_notAdmin() throws Exception {
		this.mockMvc.perform(patch("/api/admin/cards/block/2202202044507626").with(csrf()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_blockCard_authorized_notFoundCard() throws Exception {
		this.mockMvc.perform(patch("/api/admin/cards/block/2202202044507000").with(csrf()))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_blockCard_authorized_blocked() throws Exception {
		this.mockMvc.perform(patch("/api/admin/cards/block/2202202044507626").with(csrf())).andExpect(status().isOk());
	}

//	Activate cards tests

	@Test
	void test_activateCard_unauthorized() throws Exception {
		this.mockMvc.perform(patch("/api/admin/cards/activate/2202202044507626").with(csrf()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("s.petrov@test.com")
	void test_activateCard_notAdmin() throws Exception {
		this.mockMvc.perform(patch("/api/admin/cards/activate/2202202044507626").with(csrf()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_activateCard_authorized_notFoundCard() throws Exception {
		this.mockMvc.perform(patch("/api/admin/cards/activate/2202202044507000").with(csrf()))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_activateCard_authorized_activated() throws Exception {
		this.mockMvc.perform(patch("/api/admin/cards/activate/2202202044507626").with(csrf()))
				.andExpect(status().isOk());
	}

//	Get block requests

	@Test
	void test_getCardsWithBlockRequests_unauthorized() throws Exception {
		this.mockMvc.perform(get("/api/admin/cards/block-requests").with(csrf())).andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("s.petrov@test.com")
	void test_getCardsWithBlockRequests_notAdmin() throws Exception {
		this.mockMvc.perform(get("/api/admin/cards/block-requests").with(csrf())).andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_getCardsWithBlockRequests_noCardsWithBlockRequests() throws Exception {
		this.mockMvc.perform(get("/api/admin/cards/block-requests").with(csrf())).andExpect(status().isNoContent());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_getCardsWithBlockRequests_ok() throws Exception {
		Card card = cardRepository.findById(16L).orElseThrow();
		card.setIsBlockRequest(true);
		cardRepository.save(card);
		this.mockMvc.perform(get("/api/admin/cards/block-requests").with(csrf())).andExpect(status().isOk());
	}

//	Set limits tests

	@Test
	void test_setLimits_unauthorized() throws Exception {
		this.mockMvc.perform(patch("/api/admin/cards/limits").with(csrf())).andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("s.petrov@test.com")
	void test_setLimits_notAdmin() throws Exception {
		this.mockMvc.perform(patch("/api/admin/cards/limits").with(csrf())).andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_setLimits_authorized_nullValues() throws Exception {
		SetLimitsDTO object = new SetLimitsDTO("", null, null);
		this.mockMvc
				.perform(patch("/api/admin/cards/limits").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_setLimits_authorized_notValidCardNumber() throws Exception {
		SetLimitsDTO object = new SetLimitsDTO("1234123412341234", 10000.00, 10000.00);
		this.mockMvc
				.perform(patch("/api/admin/cards/limits").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_setLimits_authorized_cardNotFound() throws Exception {
		SetLimitsDTO object = new SetLimitsDTO("8484307317033105", 10000.00, 10000.00);
		this.mockMvc
				.perform(patch("/api/admin/cards/limits").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_setLimits_authorized_ok() throws Exception {
		SetLimitsDTO object = new SetLimitsDTO("2202202044507626", 10000.00, 10000.00);
		this.mockMvc
				.perform(patch("/api/admin/cards/limits").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
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
		this.mockMvc.perform(delete("/api/admin/cards/2202202044507000").with(csrf())).andExpect(status().isNotFound());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_deleteCard_authorized_deleted() throws Exception {
		this.mockMvc.perform(delete("/api/admin/cards/2202202044507626").with(csrf()))
				.andExpect(status().isNoContent());
	}

//	Get all cards tests

	@Test
	void test_getAllCards_unauthorized() throws Exception {
		this.mockMvc.perform(get("/api/admin/cards?page=1&size=10")).andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("s.petrov@test.com")
	void test_getAllCards_notAdmin() throws Exception {
		this.mockMvc.perform(get("/api/admin/cards?page=1&size=10")).andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_getAllCards_authorized_invalidQueryParams() throws Exception {
		this.mockMvc.perform(get("/api/admin/cards")).andExpect(status().isBadRequest());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_getAllCards_authorized() throws Exception {
		this.mockMvc.perform(get("/api/admin/cards?page=1&size=10")).andExpect(status().isOk());

		MvcResult result = mockMvc.perform(get("/api/admin/cards?page=1&size=10")).andReturn();
		assertNotNull(result);
		assertEquals(result.getResponse().getContentType(), "application/json");
	}

//	Get cards by user email tests

	@Test
	void test_getUserCardsByUserEmail_unauthorized() throws Exception {
		this.mockMvc.perform(get("/api/admin/cards/i.ivanov@test.com")).andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("s.petrov@test.com")
	void test_getUserCardsByUserEmail_notAdmin() throws Exception {
		this.mockMvc.perform(get("/api/admin/cards/i.ivanov@test.com")).andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_getUserCardsByUserEmail_authorized_invalidEmail() throws Exception {
		this.mockMvc.perform(get("/api/admin/cards/i.i.ivanov@test.com")).andExpect(status().isNotFound());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_getUserCardsByUserEmail_authorized() throws Exception {
		this.mockMvc.perform(get("/api/admin/cards/i.ivanov@test.com")).andExpect(status().isOk());

		MvcResult result = mockMvc.perform(get("/api/admin/cards/i.ivanov@test.com")).andReturn();
		assertNotNull(result);
		assertEquals(result.getResponse().getContentType(), "application/json");
	}

//	Get all transactions tests

	@Test
	void test_getAllTransactions_unauthorized() throws Exception {
		this.mockMvc.perform(get("/api/admin/transactions?page=1&size=10")).andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("s.petrov@test.com")
	void test_getAllTransactions_notAdmin() throws Exception {
		this.mockMvc.perform(get("/api/admin/transactions?page=1&size=10")).andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_getAllTransactions_authorized_invalidQueryParams() throws Exception {
		this.mockMvc.perform(get("/api/admin/transactions")).andExpect(status().isBadRequest());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_getAllTransactions_authorized() throws Exception {
		this.mockMvc.perform(get("/api/admin/transactions?page=1&size=10")).andExpect(status().isOk());

		MvcResult result = mockMvc.perform(get("/api/admin/transactions?page=1&size=10")).andReturn();
		assertNotNull(result);
		assertEquals(result.getResponse().getContentType(), "application/json");
	}

//	Creation users tests

	@Test
	void test_createUser_unauthorized() throws Exception {
		this.mockMvc.perform(post("/api/admin/users/create")).andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("s.petrov@test.com")
	void test_createUser_notAdmin() throws Exception {
		this.mockMvc.perform(post("/api/admin/users/create")).andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_createUser_authorized_blankFields() throws Exception {
		UserCreateDTO object = new UserCreateDTO("", "", "", "", "");
		this.mockMvc
				.perform(post("/api/admin/users/create").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_createUser_authorized_notValidEmail() throws Exception {
		UserCreateDTO object = new UserCreateDTO("Ivanov", "Ivan", "Ivanovich", "i.ivanov", "password");
		this.mockMvc
				.perform(post("/api/admin/users/create").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_createUser_authorized_notUniqueEmail() throws Exception {
		UserCreateDTO object = new UserCreateDTO("Ivanov", "Ivan", "Ivanovich", "i.ivanov@test.com", "password");
		this.mockMvc
				.perform(post("/api/admin/users/create").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isConflict());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_createUser_authorized_created() throws Exception {
		UserCreateDTO object = new UserCreateDTO("Ivanov", "Ivan", "Ivanovich", "i.i.ivanov@test.com", "password");
		this.mockMvc
				.perform(post("/api/admin/users/create").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated());
	}

//	Get users tests

	@Test
	void test_getUsers_unauthorized() throws Exception {
		this.mockMvc.perform(get("/api/admin/users?page=1&size=5")).andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("s.petrov@test.com")
	void test_getUsers_notAdmin() throws Exception {
		this.mockMvc.perform(get("/api/admin/users?page=1&size=5")).andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_getUsers_authorized_invalidQueryParams() throws Exception {
		this.mockMvc.perform(get("/api/admin/users")).andExpect(status().isBadRequest());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_getUsers_authorized() throws Exception {
		this.mockMvc.perform(get("/api/admin/users?page=1&size=5")).andExpect(status().isOk());

		MvcResult result = mockMvc.perform(get("/api/admin/users?page=1&size=5")).andReturn();
		assertNotNull(result);
		assertEquals(result.getResponse().getContentType(), "application/json");
	}

//	Add card to user test

	@Test
	void test_addCardToUser_unauthorized() throws Exception {
		this.mockMvc.perform(post("/api/admin/users/add-card")).andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("s.petrov@test.com")
	void test_addCardToUser_notAdmin() throws Exception {
		this.mockMvc.perform(post("/api/admin/users/add-card")).andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_addCardToUser_authorized_blankFields() throws Exception {
		AddCardToUserDTO object = new AddCardToUserDTO("", "");
		this.mockMvc
				.perform(post("/api/admin/users/add-card").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_addCardToUser_authorized_notValidEmail() throws Exception {
		AddCardToUserDTO object = new AddCardToUserDTO("4333780415293668", "i.i.ivanov");
		this.mockMvc
				.perform(post("/api/admin/users/add-card").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_caddCardToUser_authorized_notValidCardNumber() throws Exception {
		AddCardToUserDTO object = new AddCardToUserDTO("1234123412341234", "i.ivanov@test.com");
		this.mockMvc
				.perform(post("/api/admin/users/add-card").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_caddCardToUser_authorized_cardAlreadyHasOwner() throws Exception {
		AddCardToUserDTO object = new AddCardToUserDTO("2202202044507626", "i.ivanov@test.com");
		this.mockMvc
				.perform(post("/api/admin/users/add-card").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isConflict());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_addCardToUser_authorized_ok() throws Exception {
		AddCardToUserDTO object = new AddCardToUserDTO("4333780415293668", "i.ivanov@test.com");
		this.mockMvc
				.perform(post("/api/admin/users/add-card").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

//	Block user tests

	@Test
	void test_blockUser_unauthorized() throws Exception {
		this.mockMvc.perform(patch("/api/admin/users/block/i.ivanov@test.com").with(csrf()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("s.petrov@test.com")
	void test_blockUser_notAdmin() throws Exception {
		this.mockMvc.perform(patch("/api/admin/users/block/i.ivanov@test.com").with(csrf()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_blockUser_authorized_notFoundUser() throws Exception {
		this.mockMvc.perform(patch("/api/admin/users/block/i.i.ivanov@test.com").with(csrf()))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_blockUser_authorized_blocked() throws Exception {
		this.mockMvc.perform(patch("/api/admin/users/block/i.ivanov@test.com").with(csrf())).andExpect(status().isOk());
	}

//	Activate user tests

	@Test
	void test_activateUser_unauthorized() throws Exception {
		this.mockMvc.perform(patch("/api/admin/users/activate/i.ivanov@test.com").with(csrf()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("s.petrov@test.com")
	void test_activateUser_notAdmin() throws Exception {
		this.mockMvc.perform(patch("/api/admin/users/activate/i.ivanov@test.com").with(csrf()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_activateUser_authorized_notFoundUser() throws Exception {
		this.mockMvc.perform(patch("/api/admin/users/activate/i.i.ivanov@test.com").with(csrf()))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_activateUser_authorized_blocked() throws Exception {
		this.mockMvc.perform(patch("/api/admin/users/activate/i.ivanov@test.com").with(csrf()))
				.andExpect(status().isOk());
	}

//	Delete user tests

	@Test
	void test_deleteUser_unauthorized() throws Exception {
		this.mockMvc.perform(delete("/api/admin/users/i.ivanov@test.com").with(csrf()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("s.petrov@test.com")
	void test_deleteUser_notAdmin() throws Exception {
		this.mockMvc.perform(delete("/api/admin/users/i.ivanov@test.com").with(csrf()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_deleteUSer_authorized_notFoundUSer() throws Exception {
		this.mockMvc.perform(delete("/api/admin/users/i.i.ivanov@test.com").with(csrf()))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithUserDetails("v.sergeev@test.com")
	void test_deleteUser_authorized_deleted() throws Exception {
		this.mockMvc.perform(delete("/api/admin/users/i.ivanov@test.com").with(csrf()))
				.andExpect(status().isNoContent());
	}

}
