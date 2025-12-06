package com.dmcustoms.app.web;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dmcustoms.app.data.dto.CardCreateDTO;
import com.dmcustoms.app.data.dto.CardShowDTO;
import com.dmcustoms.app.data.dto.ResponseErrorDTO;
import com.dmcustoms.app.data.dto.UserCreateDTO;
import com.dmcustoms.app.data.dto.UserShowDTO;
import com.dmcustoms.app.data.entities.Card;
import com.dmcustoms.app.data.entities.User;
import com.dmcustoms.app.data.repositories.CardRepository;
import com.dmcustoms.app.data.repositories.UserRepository;
import com.dmcustoms.app.data.types.Authorities;
import com.dmcustoms.app.data.types.CardStatus;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@RestController
@RequestMapping(path = "/api/admin", produces = "application/json")
public class AdminController {

	private UserRepository userRepository;

	private PasswordEncoder passwordEncoder;

	private CardRepository cardRepository;

	@PostMapping("/create/card")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> createCard(@RequestBody @Valid CardCreateDTO cardCreateDTO, Errors errors) {
		if (errors.hasErrors()) {
			List<ResponseErrorDTO> messages = errors.getFieldErrors().stream()
					.map(fieldError -> new ResponseErrorDTO(fieldError.getDefaultMessage())).toList();
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(messages);
		}
		Card card = new Card(cardCreateDTO.getCardNumber(), Instant.now().plus(Duration.ofDays(1825)),
				CardStatus.ACTIVE, cardCreateDTO.getBalance(), cardCreateDTO.getLimitPerDay(),
				cardCreateDTO.getLimitPerMonth());
		try {
			Card savedCard = cardRepository.save(card);
			CardShowDTO savedCardToResponse = new CardShowDTO(savedCard.getCardNumber(), savedCard.getExpiredAt(),
					savedCard.getStatus(), savedCard.getBalance(), savedCard.getLimitPerDay(),
					savedCard.getLimitPerMonth());
			return ResponseEntity.status(HttpStatus.CREATED).body(savedCardToResponse);
		} catch (DataIntegrityViolationException e) {
			if (e.getLocalizedMessage().contains("ERROR: duplicate key value violates unique constraint"))
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(new ResponseErrorDTO("Card with number " + card.getCardNumber() + " already exist"));
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseErrorDTO(e.getMessage()));
		}
	}

	@PostMapping("/cards/block/{cardNumber}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> blockCard(@PathVariable String cardNumber) {
		Optional<Card> optionalCard = cardRepository.findCardByCardNumber(cardNumber);
		if (optionalCard.isPresent()) {
			Card card = optionalCard.get();
			card.setStatus(CardStatus.BLOCKED);
			cardRepository.save(card);
			return ResponseEntity.status(HttpStatus.OK).body(null);
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new ResponseErrorDTO("Card with card number " + cardNumber + " not found"));
		}
	}

	@PostMapping("/cards/activate/{cardNumber}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> activateCard(@PathVariable String cardNumber) {
		Optional<Card> optionalCard = cardRepository.findCardByCardNumber(cardNumber);
		if (optionalCard.isPresent()) {
			Card card = optionalCard.get();
			card.setStatus(CardStatus.ACTIVE);
			cardRepository.save(card);
			return ResponseEntity.status(HttpStatus.OK).body(null);
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new ResponseErrorDTO("Card with card number " + cardNumber + " not found"));
		}
	}

	@DeleteMapping("/cards/{cardNumber}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> deleteCard(@PathVariable String cardNumber) {
		Optional<Card> optionalCard = cardRepository.findCardByCardNumber(cardNumber);
		if (optionalCard.isPresent()) {
			cardRepository.delete(optionalCard.get());
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new ResponseErrorDTO("Card with card number " + cardNumber + " not found"));
		}
	}

	@PostMapping("/create/user")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> createUser(@RequestBody @Valid UserCreateDTO userCreateDTO, Errors errors) {
		if (errors.hasErrors()) {
			List<ResponseErrorDTO> messages = errors.getFieldErrors().stream()
					.map(fieldError -> new ResponseErrorDTO(fieldError.getDefaultMessage())).toList();
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(messages);
		}
		User user = new User(userCreateDTO.getSurname(), userCreateDTO.getName(), userCreateDTO.getLastname(),
				userCreateDTO.getEmail(), passwordEncoder.encode(userCreateDTO.getPassword()), true, true, true, true,
				Arrays.asList(Authorities.USER));
		try {
			User savedUser = userRepository.save(user);
			UserShowDTO savedUserToResponse = new UserShowDTO(savedUser.getSurname(), savedUser.getName(),
					savedUser.getLastname(), savedUser.getEmail());
			return ResponseEntity.status(HttpStatus.CREATED).body(savedUserToResponse);
		} catch (DataIntegrityViolationException e) {
			if (e.getLocalizedMessage().contains("ERROR: duplicate key value violates unique constraint"))
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(new ResponseErrorDTO("User with email " + user.getEmail() + " already exist"));
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseErrorDTO(e.getMessage()));
		}
	}

	@GetMapping("/users")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> showUsers() {
		List<User> usersFromDB = userRepository.findAll().stream()
				.filter(user -> user.getAuthorities().contains(Authorities.USER)).toList();
		if (usersFromDB.isEmpty())
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
		List<UserShowDTO> usersToResponse = new ArrayList<UserShowDTO>();
		for (User user : usersFromDB) {
			usersToResponse
					.add(new UserShowDTO(user.getSurname(), user.getName(), user.getLastname(), user.getEmail()));
		}
		return ResponseEntity.status(HttpStatus.OK).body(usersToResponse);
	}

}
