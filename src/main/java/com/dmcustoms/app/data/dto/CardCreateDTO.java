package com.dmcustoms.app.data.dto;

import org.hibernate.validator.constraints.CreditCardNumber;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CardCreateDTO {
	
	@NotBlank(message = "Card number is required")
	@CreditCardNumber(message = "Credit card number is incorrect")
	private final String cardNumber;

	@NotNull(message = "Balance is required")
	@PositiveOrZero(message = "Must be most or equals zero")
	private final Double balance;

	@NotNull(message = "Limit per day is required")
	@PositiveOrZero(message = "Must be most or equals zero")
	private final Double limitPerDay;

	@NotNull(message = "Limit per month is required")
	@PositiveOrZero(message = "Must be most or equals zero")
	private final Double limitPerMonth;
	
}
