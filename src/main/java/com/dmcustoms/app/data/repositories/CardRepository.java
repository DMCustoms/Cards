package com.dmcustoms.app.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dmcustoms.app.data.entities.Card;

public interface CardRepository extends JpaRepository<Card, Long> {
}
