package com.dmcustoms.app.data.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dmcustoms.app.data.entities.Card;
import com.dmcustoms.app.data.entities.User;

public interface CardRepository extends JpaRepository<Card, Long> {
	
	public List<Card> findCardsByOwner(User owner);
	
	public Optional<Card> findCardByCardNumber(String cardNumber);
	
}
