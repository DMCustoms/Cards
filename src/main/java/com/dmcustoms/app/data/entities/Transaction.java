package com.dmcustoms.app.data.entities;

import java.time.Instant;

import com.dmcustoms.app.data.types.TransactionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@Entity(name = "transactions")
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE, force = true)
public class Transaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "card_source", nullable = false)
	private final Card source;
	
	@ManyToOne
	@JoinColumn(name = "card_recipient")
	private final Card recipient;
	
	@Column(name = "transaction_type", nullable = false)
	private final TransactionType type;
	
	@Column(name = "transaction_date", nullable = false)
	private final Instant date;
	
	@Column(name = "transaction_value", nullable = false)
	private final Double value;
	
}
