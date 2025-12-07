package com.dmcustoms.app.data.dto;

import org.hibernate.validator.constraints.CreditCardNumber;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AddCardToUserDTO {

	@NotBlank(message = "Card number is required")
	@CreditCardNumber(message = "Credit card number is incorrect")
	private final String cardNumber;
	
	@NotBlank(message = "Email is required")
	@Email(message = "Must be in Email format")
	private final String email;
	
}
