package com.dmcustoms.app.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dmcustoms.app.data.entities.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
