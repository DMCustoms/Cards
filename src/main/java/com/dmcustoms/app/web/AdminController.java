package com.dmcustoms.app.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dmcustoms.app.data.dto.ResponseErrorDTO;
import com.dmcustoms.app.data.dto.UserCreateDTO;
import com.dmcustoms.app.data.dto.UserShowDTO;
import com.dmcustoms.app.data.entities.User;
import com.dmcustoms.app.data.repositories.UserRepository;
import com.dmcustoms.app.data.types.Authorities;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@RestController
@RequestMapping(path = "/api/admin", produces = "application/json")
public class AdminController {

	private UserRepository userRepository;

	private PasswordEncoder passwordEncoder;

	@GetMapping("/users")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> showUsers() {
		List<User> usersFromDB = userRepository.findAll().stream()
				.filter(user -> user.getAuthorities().contains(Authorities.USER)).toList();
		if (usersFromDB.isEmpty())
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
		List<UserShowDTO> usersToResponse = new ArrayList<UserShowDTO>();
		for (User user : usersFromDB) {
			usersToResponse
					.add(new UserShowDTO(user.getSurname(), user.getName(), user.getLastname(), user.getEmail()));
		}
		return ResponseEntity.status(HttpStatus.OK).body(usersToResponse);
	}

	@PostMapping("/create/user")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> createUser(@RequestBody @Valid UserCreateDTO userCreateDTO, Errors errors) {
		if (errors.hasErrors()) {
			List<ResponseErrorDTO> messages = errors.getFieldErrors().stream()
					.map(fieldError -> new ResponseErrorDTO(fieldError.getDefaultMessage())).toList();
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(messages);
		}
		User user = new User(userCreateDTO.getSurname(), userCreateDTO.getName(), userCreateDTO.getLastname(),
				userCreateDTO.getEmail(), passwordEncoder.encode(userCreateDTO.getPassword()), true, true, true, true,
				Arrays.asList(Authorities.USER));
		try {
			User savedUser = userRepository.save(user);
			UserShowDTO savedUserToResponse = new UserShowDTO(savedUser.getSurname(), savedUser.getName(),
					savedUser.getLastname(), savedUser.getEmail());
			return ResponseEntity.status(HttpStatus.OK).body(savedUserToResponse);
		} catch (DataIntegrityViolationException e) {
			if (e.getLocalizedMessage().contains("ERROR: duplicate key value violates unique constraint"))
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(new ResponseErrorDTO("User with email " + user.getEmail() + " already exist"));
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseErrorDTO(e.getMessage()));
		}
	}

}
