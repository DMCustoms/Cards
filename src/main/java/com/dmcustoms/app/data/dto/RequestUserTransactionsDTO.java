package com.dmcustoms.app.data.dto;

import org.hibernate.validator.constraints.CreditCardNumber;

public record RequestUserTransactionsDTO(
		@CreditCardNumber(message = "Incorrect credit card number") String cardNumber) {

}
