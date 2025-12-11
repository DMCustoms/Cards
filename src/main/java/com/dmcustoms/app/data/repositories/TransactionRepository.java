package com.dmcustoms.app.data.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.dmcustoms.app.data.entities.Card;
import com.dmcustoms.app.data.entities.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	public List<Transaction> findTransactionsBySource(Card source);

	public Page<Transaction> findTransactionsBySource(Card source, Pageable pageable);

}
