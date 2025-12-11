package com.dmcustoms.app.web;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dmcustoms.app.data.dto.CardShowDTO;
import com.dmcustoms.app.data.dto.RequestUserTransactionsDTO;
import com.dmcustoms.app.data.dto.ResponseErrorDTO;
import com.dmcustoms.app.data.dto.TransactionDTO;
import com.dmcustoms.app.data.dto.TransferDTO;
import com.dmcustoms.app.data.dto.WriteOffDTO;
import com.dmcustoms.app.data.entities.Card;
import com.dmcustoms.app.data.entities.Transaction;
import com.dmcustoms.app.data.entities.User;
import com.dmcustoms.app.data.repositories.CardRepository;
import com.dmcustoms.app.data.repositories.TransactionRepository;
import com.dmcustoms.app.data.types.TransactionType;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@RestController
@AllArgsConstructor
@RequestMapping(path = "/api/user", produces = "application/json")
public class UserController {

	private CardRepository cardRepository;

	private TransactionRepository transactionRepository;

	@GetMapping("/cards")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<?> showUserCards(@AuthenticationPrincipal User user) {
		List<Card> userCardsFromDB = cardRepository.findCardsByOwner(user);
		if (userCardsFromDB.isEmpty())
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
		List<CardShowDTO> userCardsToResponse = new ArrayList<CardShowDTO>();
		for (Card card : userCardsFromDB) {
			userCardsToResponse.add(new CardShowDTO(card.getCardNumber(), card.getExpiredAt(), card.getStatus(),
					card.getBalance(), card.getLimitPerDay(), card.getLimitPerMonth()));
		}
		return ResponseEntity.status(HttpStatus.OK).body(userCardsToResponse);
	}

	@GetMapping("/transactions")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<?> getTransactions(@AuthenticationPrincipal User user,
			@RequestParam Map<String, String> params,
			@RequestBody @Valid RequestUserTransactionsDTO requestUserTransactionsDTO, Errors errors) {
		if (errors.hasErrors()) {
			List<ResponseErrorDTO> messages = errors.getFieldErrors().stream()
					.map(fieldError -> new ResponseErrorDTO(fieldError.getDefaultMessage())).toList();
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(messages);
		}
		String cardNumber = requestUserTransactionsDTO.cardNumber();
		Optional<Card> optionalCard = cardRepository.findCardByCardNumber(cardNumber);
		if (optionalCard.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new ResponseErrorDTO("Card with card number " + cardNumber + " not found"));
		}
		Card card = optionalCard.get();
		if (!card.getOwner().getEmail().equals(user.getEmail())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
					new ResponseErrorDTO("USer with email " + user.getEmail() + " not owner of card " + cardNumber));
		}
		String page = params.get("page");
		String size = params.get("size");
		if (page != null && size != null) {
			PageRequest pageRequest = PageRequest.of(Integer.valueOf(page), Integer.valueOf(size));
			List<Transaction> transactionsPage = transactionRepository.findTransactionsBySource(card, pageRequest)
					.getContent();
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

	@PatchMapping("/block/{cardNumber}")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<?> requestBlockCard(@PathVariable String cardNumber, @AuthenticationPrincipal User user) {
		Optional<Card> optionalCard = cardRepository.findCardByCardNumber(cardNumber);
		if (optionalCard.isPresent()) {
			Card card = optionalCard.get();
			if (card.getOwner().equals(user)) {
				card.setIsBlockRequest(true);
				cardRepository.save(card);
				return ResponseEntity.status(HttpStatus.OK).body(null);
			} else {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseErrorDTO(
						"User with email " + user.getEmail() + " is not owner of card " + cardNumber));
			}
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new ResponseErrorDTO("Card with cardnumber " + cardNumber + " not found"));
		}
	}

	@PostMapping("/transfer")
	@PreAuthorize("hasRole('USER')")
	@Transactional
	public ResponseEntity<?> transfer(@RequestBody @Valid TransferDTO transferDTO, Errors errors,
			@AuthenticationPrincipal User user) {
		if (errors.hasErrors()) {
			List<ResponseErrorDTO> messages = errors.getFieldErrors().stream()
					.map(fieldError -> new ResponseErrorDTO(fieldError.getDefaultMessage())).toList();
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(messages);
		}
		String cardSourceNumber = transferDTO.getCardSourceNumber();
		String cardRecipientNumber = transferDTO.getCardRecipientNumber();
		Double transferValue = transferDTO.getValue();
		Optional<Card> cardSourceOptional = cardRepository.findCardByCardNumber(cardSourceNumber);
		Optional<Card> cardRecipientOptional = cardRepository.findCardByCardNumber(cardRecipientNumber);
		if (cardSourceOptional.isEmpty())
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new ResponseErrorDTO("Card with card number " + cardSourceNumber + " is not found"));
		if (cardRecipientOptional.isEmpty())
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new ResponseErrorDTO("Card with card number " + cardRecipientNumber + " is not found"));
		Card cardSource = cardSourceOptional.get();
		Card cardRecipient = cardRecipientOptional.get();
		if (!cardSource.getOwner().getEmail().equals(user.getEmail()))
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseErrorDTO(
					"User with email " + user.getEmail() + " is not owner of card " + cardSourceNumber));
		if (!cardRecipient.getOwner().getEmail().equals(user.getEmail()))
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseErrorDTO(
					"User with email " + user.getEmail() + " is not owner of card " + cardRecipientNumber));
		if (cardSource.getBalance() < transferValue)
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new ResponseErrorDTO("Insufficient funds on the card"));
		cardSource.setBalance(Math.round((cardSource.getBalance() - transferValue) * 100.0) / 100.0);
		cardRecipient.setBalance(Math.round((cardRecipient.getBalance() + transferValue) * 100.0) / 100.0);
		cardRepository.save(cardSource);
		cardRepository.save(cardRecipient);
		Transaction transaction = new Transaction(cardSource, cardRecipient, TransactionType.TRANSFER, Instant.now(),
				transferValue);
		transactionRepository.save(transaction);
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	@PostMapping("/writeoff")
	@PreAuthorize("hasRole('USER')")
	@Transactional
	public ResponseEntity<?> writeOff(@RequestBody @Valid WriteOffDTO writeOffDTO, Errors errors,
			@AuthenticationPrincipal User user) {
		if (errors.hasErrors()) {
			List<ResponseErrorDTO> messages = errors.getFieldErrors().stream()
					.map(fieldError -> new ResponseErrorDTO(fieldError.getDefaultMessage())).toList();
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(messages);
		}
		String cardNumber = writeOffDTO.getCardNumber();
		Double writeOffValue = writeOffDTO.getValue();
		Optional<Card> cardOptional = cardRepository.findCardByCardNumber(cardNumber);
		if (cardOptional.isEmpty())
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new ResponseErrorDTO("Card with card number " + cardNumber + " is not found"));
		Card card = cardOptional.get();
		if (!card.getOwner().getEmail().equals(user.getEmail()))
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
					new ResponseErrorDTO("User with email " + user.getEmail() + " is not owner of card " + cardNumber));
		if (card.getBalance() < writeOffValue)
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new ResponseErrorDTO("Insufficient funds on the card"));
		LocalDate now = LocalDate.now();
		Double sumOfOperationsOnThisDay = transactionRepository.findTransactionsBySource(card).stream()
				.filter(transaction -> LocalDate.ofInstant(transaction.getDate(), ZoneId.systemDefault()).equals(now))
				.mapToDouble(Transaction::getValue).sum();
		if (sumOfOperationsOnThisDay + writeOffValue > card.getLimitPerDay()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new ResponseErrorDTO("The user has exceeded the daily transaction limit."));
		}
		Double sumOfOperationsOnThisMonth = transactionRepository
				.findTransactionsBySource(card).stream().filter(transaction -> LocalDate
						.ofInstant(transaction.getDate(), ZoneId.systemDefault()).getMonth().equals(now.getMonth()))
				.mapToDouble(Transaction::getValue).sum();
		if (sumOfOperationsOnThisMonth + writeOffValue > card.getLimitPerMonth()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new ResponseErrorDTO("The user has exceeded the monthly transaction limit."));
		}
		card.setBalance(Math.round((card.getBalance() - writeOffValue) * 100.0) / 100.0);
		cardRepository.save(card);
		Transaction transaction = new Transaction(card, null, TransactionType.WRITEOFF, Instant.now(), writeOffValue);
		transactionRepository.save(transaction);
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

}
