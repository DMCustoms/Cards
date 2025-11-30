package com.dmcustoms.app.data.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserCreateDTO {

	@NotBlank(message = "Surname is required")
	private final String surname;

	@NotBlank(message = "Name is required")
	private final String name;

	@NotBlank(message = "Lastname is required")
	private final String lastname;

	@NotBlank(message = "Email is required")
	@Email(message = "Must be in Email format")
	private final String email;

	@NotBlank(message = "Password is required")
	private final String password;
	
}
