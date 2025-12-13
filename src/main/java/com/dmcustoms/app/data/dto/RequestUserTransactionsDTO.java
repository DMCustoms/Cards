package com.dmcustoms.app.data.dto;

import org.hibernate.validator.constraints.CreditCardNumber;

import jakarta.validation.constraints.NotBlank;

public record RequestUserTransactionsDTO(
		@CreditCardNumber(message = "Card number is incorrect") @NotBlank(message = "Card number is required") String cardNumber) {
}
