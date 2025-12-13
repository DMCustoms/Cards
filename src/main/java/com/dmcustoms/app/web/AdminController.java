package com.dmcustoms.app.web;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dmcustoms.app.data.dto.AddCardToUserDTO;
import com.dmcustoms.app.data.dto.CardCreateDTO;
import com.dmcustoms.app.data.dto.CardShowDTO;
import com.dmcustoms.app.data.dto.ResponseErrorDTO;
import com.dmcustoms.app.data.dto.SetLimitsDTO;
import com.dmcustoms.app.data.dto.TransactionDTO;
import com.dmcustoms.app.data.dto.UserCreateDTO;
import com.dmcustoms.app.data.dto.UserShowDTO;
import com.dmcustoms.app.data.entities.Card;
import com.dmcustoms.app.data.entities.Transaction;
import com.dmcustoms.app.data.entities.User;
import com.dmcustoms.app.data.repositories.CardRepository;
import com.dmcustoms.app.data.repositories.TransactionRepository;
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

	private TransactionRepository transactionRepository;

	@PostMapping("/cards/create")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> createCard(@RequestBody @Valid CardCreateDTO cardCreateDTO, Errors errors) {
		if (errors.hasErrors()) {
			List<ResponseErrorDTO> messages = errors.getFieldErrors().stream()
					.map(fieldError -> new ResponseErrorDTO(fieldError.getDefaultMessage())).toList();
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(messages);
		}
		Card card = new Card(cardCreateDTO.getCardNumber(), Instant.now().plus(Duration.ofDays(1825)),
				CardStatus.ACTIVE, cardCreateDTO.getBalance(), cardCreateDTO.getLimitPerDay(),
				cardCreateDTO.getLimitPerMonth(), false);
		try {
			cardRepository.save(card);
			return ResponseEntity.status(HttpStatus.CREATED).body(null);
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(
					new ResponseErrorDTO("Card with card number " + cardCreateDTO.getCardNumber()) + " already exist");
		}
	}

	@PatchMapping("/cards/block/{cardNumber}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> blockCard(@PathVariable String cardNumber) {
		Optional<Card> optionalCard = cardRepository.findCardByCardNumber(cardNumber);
		if (optionalCard.isPresent()) {
			Card card = optionalCard.get();
			card.setStatus(CardStatus.BLOCKED);
			cardRepository.save(card);
			return ResponseEntity.status(HttpStatus.OK).body(null);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ResponseErrorDTO("Card with card number " + cardNumber + " not found"));
		}
	}

	@PatchMapping("/cards/activate/{cardNumber}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> activateCard(@PathVariable String cardNumber) {
		Optional<Card> optionalCard = cardRepository.findCardByCardNumber(cardNumber);
		if (optionalCard.isPresent()) {
			Card card = optionalCard.get();
			card.setStatus(CardStatus.ACTIVE);
			cardRepository.save(card);
			return ResponseEntity.status(HttpStatus.OK).body(null);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ResponseErrorDTO("Card with card number " + cardNumber + " not found"));
		}
	}

	@GetMapping("/cards/block-requests")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> getCardsWithBlockRequests() {
		List<Card> cardsWithBlockRequest = cardRepository.findCardsByIsBlockRequest(true);
		if (!cardsWithBlockRequest.isEmpty()) {
			List<CardShowDTO> cardsToResponse = new ArrayList<CardShowDTO>();
			for (Card card : cardsWithBlockRequest) {
				cardsToResponse.add(new CardShowDTO(card.getCardNumber(), card.getExpiredAt(), card.getStatus(),
						card.getBalance(), card.getLimitPerDay(), card.getLimitPerMonth()));
			}
			return ResponseEntity.status(HttpStatus.OK).body(cardsToResponse);
		} else {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
		}
	}

	@PatchMapping("/cards/limits")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> setLimits(@RequestBody @Valid SetLimitsDTO setLimitsDTO, Errors errors) {
		if (errors.hasErrors()) {
			List<ResponseErrorDTO> messages = errors.getFieldErrors().stream()
					.map(fieldError -> new ResponseErrorDTO(fieldError.getDefaultMessage())).toList();
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(messages);
		}
		Optional<Card> optionalCard = cardRepository.findCardByCardNumber(setLimitsDTO.getCardNumber());
		if (optionalCard.isPresent()) {
			Card card = optionalCard.get();
			double limitPerDay = setLimitsDTO.getLimitPerDay();
			double limitPerMonth = setLimitsDTO.getLimitPerMonth();
			limitPerDay = Math.round(limitPerDay * 100.0) / 100.0;
			limitPerMonth = Math.round(limitPerMonth * 100.0) / 100.0;
			card.setLimitPerDay(limitPerDay);
			card.setLimitPerMonth(limitPerMonth);
			cardRepository.save(card);
			return ResponseEntity.status(HttpStatus.OK).body(null);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ResponseErrorDTO("Card with card number " + setLimitsDTO.getCardNumber() + " not found"));
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
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ResponseErrorDTO("Card with card number " + cardNumber + " not found"));
		}
	}

	@GetMapping("/cards")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> getAllCards(@RequestParam Map<String, String> params) {
		String page = params.get("page");
		String size = params.get("size");
		if (page != null && size != null) {
			PageRequest pageRequest = PageRequest.of(Integer.valueOf(page), Integer.valueOf(size));
			List<Card> cardsPage = cardRepository.findAll(pageRequest).getContent();
			List<CardShowDTO> cardsToResponse = new ArrayList<CardShowDTO>();
			for (Card card : cardsPage) {
				cardsToResponse.add(new CardShowDTO(card.getCardNumber(), card.getExpiredAt(), card.getStatus(),
						card.getBalance(), card.getLimitPerDay(), card.getLimitPerMonth()));
			}
			return ResponseEntity.status(HttpStatus.OK).body(cardsToResponse);
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new ResponseErrorDTO("Query parameters are invalid"));
		}
	}

	@GetMapping("/cards/{email}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> getCardsByUserEmail(@PathVariable String email) {
		Optional<User> user = userRepository.findUserByEmail(email);
		if (user.isEmpty())
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		List<Card> findedCardsByOwner = cardRepository.findCardsByOwner(user.get());
		List<CardShowDTO> cardsToResponse = new ArrayList<CardShowDTO>();
		for (Card card : findedCardsByOwner) {
			cardsToResponse.add(new CardShowDTO(card.getCardNumber(), card.getExpiredAt(), card.getStatus(),
					card.getBalance(), card.getLimitPerDay(), card.getLimitPerMonth()));
		}
		return ResponseEntity.status(HttpStatus.OK).body(cardsToResponse);
	}

	@GetMapping("/transactions")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> getAllTransactions(@RequestParam Map<String, String> params) {
		String page = params.get("page");
		String size = params.get("size");
		if (page != null && size != null) {
			PageRequest pageRequest = PageRequest.of(Integer.valueOf(page), Integer.valueOf(size));
			List<Transaction> transactionsPage = transactionRepository.findAll(pageRequest).getContent();
			List<TransactionDTO> transactionsToResponse = new ArrayList<TransactionDTO>();
			for (Transaction transaction : transactionsPage) {
				transactionsToResponse.add(new TransactionDTO(transaction.getSource().getCardNumber(),
						transaction.getRecipient() == null ? null : transaction.getRecipient().getCardNumber(),
						transaction.getType(), transaction.getDate(), transaction.getValue()));
			}
			return ResponseEntity.status(HttpStatus.OK).body(transactionsToResponse);
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new ResponseErrorDTO("Query parameters are invalid"));
		}
	}

	@PostMapping("/users/create")
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
			userRepository.save(user);
			return ResponseEntity.status(HttpStatus.CREATED).body(null);
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(new ResponseErrorDTO("User with email " + userCreateDTO.getEmail() + " already exist"));
		}
	}

	@GetMapping("/users")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> getAllUsers(@RequestParam Map<String, String> params) {
		String page = params.get("page");
		String size = params.get("size");
		if (page != null && size != null) {
			PageRequest pageRequest = PageRequest.of(Integer.valueOf(page), Integer.valueOf(size));
			List<User> userPage = userRepository.findAll(pageRequest).getContent();
			List<UserShowDTO> usersToResponse = new ArrayList<UserShowDTO>();
			for (User user : userPage) {
				usersToResponse
						.add(new UserShowDTO(user.getSurname(), user.getName(), user.getLastname(), user.getEmail()));
			}
			return ResponseEntity.status(HttpStatus.OK).body(usersToResponse);
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new ResponseErrorDTO("Query parameters are invalid"));
		}
	}

	@PostMapping("/users/add-card")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> addCardToUser(@RequestBody @Valid AddCardToUserDTO addCardToUserDTO, Errors errors) {
		if (errors.hasErrors()) {
			List<ResponseErrorDTO> messages = errors.getFieldErrors().stream()
					.map(fieldError -> new ResponseErrorDTO(fieldError.getDefaultMessage())).toList();
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(messages);
		}
		Optional<User> optionalUser = userRepository.findUserByEmail(addCardToUserDTO.getEmail());
		if (optionalUser.isEmpty())
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ResponseErrorDTO("User with Email " + addCardToUserDTO.getEmail() + " not found"));
		Optional<Card> optionalCard = cardRepository.findCardByCardNumber(addCardToUserDTO.getCardNumber());
		if (optionalCard.isEmpty())
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
					new ResponseErrorDTO("Card with card number " + addCardToUserDTO.getCardNumber() + " not found"));
		Card card = optionalCard.get();
		if (card.getOwner() != null)
			return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseErrorDTO("Card with card number "
					+ addCardToUserDTO.getCardNumber() + " already has an owner: " + card.getOwner().getEmail()));
		User user = optionalUser.get();
		user.addCard(card);
		cardRepository.save(card);
		userRepository.save(user);
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	@PatchMapping("/users/block/{email}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> blockUser(@PathVariable String email) {
		Optional<User> optionalUser = userRepository.findUserByEmail(email);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			user.setIsAccountNonLocked(false);
			userRepository.save(user);
			return ResponseEntity.status(HttpStatus.OK).body(null);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ResponseErrorDTO("User with email " + email + " not found"));
		}
	}

	@PatchMapping("/users/activate/{email}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> activateUser(@PathVariable String email) {
		Optional<User> optionalUser = userRepository.findUserByEmail(email);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			user.setIsAccountNonLocked(true);
			userRepository.save(user);
			return ResponseEntity.status(HttpStatus.OK).body(null);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ResponseErrorDTO("User with email " + email + " not found"));
		}
	}

	@DeleteMapping("/users/{email}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> deleteUser(@PathVariable String email) {
		Optional<User> optionalUser = userRepository.findUserByEmail(email);
		if (optionalUser.isPresent()) {
			userRepository.delete(optionalUser.get());
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ResponseErrorDTO("User with email " + email + " not found"));
		}
	}

}
