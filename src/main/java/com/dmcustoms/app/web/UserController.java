package com.dmcustoms.app.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dmcustoms.app.data.dto.CardShowDTO;
import com.dmcustoms.app.data.dto.ResponseErrorDTO;
import com.dmcustoms.app.data.entities.Card;
import com.dmcustoms.app.data.entities.User;
import com.dmcustoms.app.data.repositories.CardRepository;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@RestController
@AllArgsConstructor
@RequestMapping(path = "/api/user", produces = "application/json")
public class UserController {

	private CardRepository cardRepository;

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

	@PatchMapping("/block/{cardNumber}")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<?> requestBlockCard(@PathVariable String cardNumber, @AuthenticationPrincipal User user) {
		Optional<Card> optionalCard = cardRepository.findCardByCardNumber(cardNumber);
		if (optionalCard.isPresent()) {
			Card card = optionalCard.get();
			if (card.getOwner().getEmail().equals(user.getEmail())) {
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

}
