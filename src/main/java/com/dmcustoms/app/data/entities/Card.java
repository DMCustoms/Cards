package com.dmcustoms.app.data.entities;

import java.time.Instant;

import com.dmcustoms.app.data.encryption.AttributeEncryptor;
import com.dmcustoms.app.data.types.CardStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@Entity(name = "cards")
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE, force = true)
public class Card {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "card_number", nullable = false, length = 128)
	@Convert(converter = AttributeEncryptor.class)
	private final String cardNumber;
	
	@Column(name = "expired_at", nullable = false)
	private final Instant expiredAt;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "card_owner")
	private User owner;
	
	@Column(name = "card_status", nullable = false)
	@NonNull
	private CardStatus status;
	
	@Column(name = "card_balance", nullable = false)
	@NonNull
	private Double balance;
	
	@Column(name = "limit_per_day")
	@NonNull
	private Double limitPerDay;
	
	@Column(name = "limit_per_month")
	@NonNull
	private Double LimitPerMonth;
	
	@Column(name = "is_block_request")
	@NonNull
	private Boolean isBlockRequest;
	
}
