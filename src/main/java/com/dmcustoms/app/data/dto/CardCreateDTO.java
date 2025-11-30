package com.dmcustoms.app.data.dto;

import org.hibernate.validator.constraints.CreditCardNumber;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CardCreateDTO {
	
	@NotBlank(message = "Card number is required")
	@CreditCardNumber(message = "Credit card number is incorrect")
	private final String cardNumber;

	@NotBlank(message = "Balance is required")
	@PositiveOrZero
	private final Double balance;

	@NotBlank(message = "Limit per day is required")
	@PositiveOrZero
	private final Double limitPerDay;

	@NotBlank(message = "Limit per month is required")
	@PositiveOrZero
	private final Double LimitPerMonth;
	
}
