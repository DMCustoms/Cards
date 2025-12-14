package com.dmcustoms.app.data.dto;

import java.time.Instant;

import com.dmcustoms.app.data.types.CardStatus;

public record CardShowDTO(String cardNumber, Instant expiredAt, CardStatus status, Double balance,
		Double limitPerDay, Double LimitPerMonth, String ownerEmail) {
}
