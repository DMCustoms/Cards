package com.dmcustoms.app.data.dto;

import org.hibernate.validator.constraints.CreditCardNumber;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WriteOffDTO {

	@NotBlank(message = "Card number is required")
	@CreditCardNumber(message = "Card number is incorrect")
	private final String cardNumber;
	
	@NotNull(message = "Value is required")
	@PositiveOrZero(message = "Must be most or equals zero")
	private final Double value;
	
}
