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
import com.dmcustoms.app.data.dto.WriteOffDTO;

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
	@WithUserDetails("o.solomatin@test.com")
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
		this.mockMvc.perform(patch("/api/user/block/2202202044507626").with(csrf())).andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("s.petrov@test.com")
	void test_requestBlockCard_authorized_notFoundCard() throws Exception {
		this.mockMvc.perform(patch("/api/user/block/2653035107502819").with(csrf())).andExpect(status().isNotFound());
	}

	@Test
	@WithUserDetails("o.solomatin@test.com")
	void test_requestBlockCard_authorized_ok() throws Exception {
		this.mockMvc.perform(patch("/api/user/block/2202202044507626").with(csrf())).andExpect(status().isOk());
	}

//	Get transactions tests

	@Test
	void test_getTransactions_unauthorized() throws Exception {
		this.mockMvc.perform(get("/api/user/transactions/7634768028741925?page=1&size=10"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("o.solomatin@test.com")
	void test_getTransactions_authorized_cardIsNotFound() throws Exception {
		this.mockMvc.perform(get("/api/user/transactions/4922446341334532?page=1&size=10").with(csrf()))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithUserDetails("e.levchenko@test.com")
	void test_getTransactions_authorized_userIsNotOwnerOfCard() throws Exception {
		this.mockMvc.perform(get("/api/user/transactions/2202202044507626?page=1&size=10").with(csrf()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("o.solomatin@test.com")
	void test_getTransactions_authorized_invalidQueryParams() throws Exception {
		this.mockMvc.perform(get("/api/user/transactions/2202202044507626").with(csrf()))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithUserDetails("o.solomatin@test.com")
	void test_getTransactions_authorized_ok() throws Exception {
		this.mockMvc.perform(get("/api/user/transactions/2202202044507626?page=1&size=10").with(csrf()))
				.andExpect(status().isOk());
	}

//	Transfer between user cards tests

	@Test
	void test_transfer_unauthorized() throws Exception {
		this.mockMvc.perform(post("/api/user/transfer")).andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("o.solomatin@test.com")
	void test_transfer_authorized_blankFields() throws Exception {
		TransferDTO object = new TransferDTO("", "", null);
		this.mockMvc
				.perform(post("/api/user/transfer").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithUserDetails("o.solomatin@test.com")
	void test_transfer_authorized_notValidFields() throws Exception {
		TransferDTO object = new TransferDTO("1234123412341234", "1234123412341234", -1.);
		this.mockMvc
				.perform(post("/api/user/transfer").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithUserDetails("o.solomatin@test.com")
	void test_transfer_authorized_cardOneIsNotFound() throws Exception {
		TransferDTO object = new TransferDTO("4922446341334532", "2202202044507626", 1500.00);
		this.mockMvc
				.perform(post("/api/user/transfer").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithUserDetails("o.solomatin@test.com")
	void test_transfer_authorized_cardTwoIsNotFound() throws Exception {
		TransferDTO object = new TransferDTO("2202202044507626", "4922446341334532", 1500.00);
		this.mockMvc
				.perform(post("/api/user/transfer").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithUserDetails("o.solomatin@test.com")
	void test_transfer_authorized_cardOneIsBlocked() throws Exception {
		TransferDTO object = new TransferDTO("3010570969331598", "2202202044507626", 1500.00);
		this.mockMvc
				.perform(post("/api/user/transfer").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("o.solomatin@test.com")
	void test_transfer_authorized_cardTwoIsBlocked() throws Exception {
		TransferDTO object = new TransferDTO("3010570969331598", "3010570969331598", 1500.00);
		this.mockMvc
				.perform(post("/api/user/transfer").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("o.solomatin@test.com")
	void test_transfer_authorized_userIsNotOwnerOfCardOne() throws Exception {
		TransferDTO object = new TransferDTO("7278005134684082", "2202202044507626", 1500.00);
		this.mockMvc
				.perform(post("/api/user/transfer").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("o.solomatin@test.com")
	void test_transfer_authorized_userIsNotOwnerOfCardTwo() throws Exception {
		TransferDTO object = new TransferDTO("2202202044507626", "7278005134684082", 1500.00);
		this.mockMvc
				.perform(post("/api/user/transfer").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("o.solomatin@test.com")
	void test_transfer_authorized_insufficientFunds() throws Exception {
		TransferDTO object = new TransferDTO("2202202044507626", "7634768028741925", 25000.00);
		this.mockMvc
				.perform(post("/api/user/transfer").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithUserDetails("o.solomatin@test.com")
	void test_transfer_authorized_ok() throws Exception {
		TransferDTO object = new TransferDTO("2202202044507626", "7634768028741925", 1500.00);
		this.mockMvc
				.perform(post("/api/user/transfer").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

//	Write off tests

	@Test
	void test_wtiteOff_unauthorized() throws Exception {
		this.mockMvc.perform(post("/api/user/writeoff")).andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("e.levchenko@test.com")
	void test_writeOff_authorized_blankFields() throws Exception {
		WriteOffDTO object = new WriteOffDTO("", null);
		this.mockMvc
				.perform(post("/api/user/writeoff").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithUserDetails("e.levchenko@test.com")
	void test_writeOff_authorized_notValidFields() throws Exception {
		WriteOffDTO object = new WriteOffDTO("1234123412341234", -1.);
		this.mockMvc
				.perform(post("/api/user/writeoff").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithUserDetails("e.levchenko@test.com")
	void test_writeOff_authorized_cardIsNotFound() throws Exception {
		WriteOffDTO object = new WriteOffDTO("9600530939276052", 1000.00);
		this.mockMvc
				.perform(post("/api/user/writeoff").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithUserDetails("o.solomatin@test.com")
	void test_writeOff_authorized_cardIsBlocked() throws Exception {
		WriteOffDTO object = new WriteOffDTO("3010570969331598", 1000.00);
		this.mockMvc
				.perform(post("/api/user/writeoff").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("e.levchenko@test.com")
	void test_writeOff_authorized_userIsNotOwnerOfCard() throws Exception {
		WriteOffDTO object = new WriteOffDTO("2202202044507626", 1000.00);
		this.mockMvc
				.perform(post("/api/user/writeoff").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails("e.levchenko@test.com")
	void test_writeOff_authorized_insufficientFunds() throws Exception {
		WriteOffDTO object = new WriteOffDTO("7278005134684082", 150000.00);
		this.mockMvc
				.perform(post("/api/user/writeoff").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithUserDetails("e.levchenko@test.com")
	void test_writeOff_authorized_exceededLimit() throws Exception {
		WriteOffDTO object = new WriteOffDTO("7278005134684082", 80000.00);
		this.mockMvc
				.perform(post("/api/user/writeoff").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithUserDetails("e.levchenko@test.com")
	void test_writeOff_authorized_ok() throws Exception {
		WriteOffDTO object = new WriteOffDTO("7278005134684082", 10000.00);
		this.mockMvc
				.perform(post("/api/user/writeoff").with(csrf()).contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(object)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

}
