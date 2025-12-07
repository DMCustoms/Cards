package com.dmcustoms.app.data.dto;

import java.time.Instant;

import com.dmcustoms.app.data.types.TransactionType;

public record TransactionDTO(String cardSourceNumber, String cardRecipientNumber, TransactionType type, Instant date,
		Double value) {
}
