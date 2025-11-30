package com.dmcustoms.app.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dmcustoms.app.data.dto.UserShowDTO;
import com.dmcustoms.app.data.entities.User;
import com.dmcustoms.app.data.repositories.UserRepository;
import com.dmcustoms.app.data.types.Authorities;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@RestController
@RequestMapping(path = "/api/admin", produces = "application/json")
public class AdminController {

	private UserRepository userRepository;

	@GetMapping("/users")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> showUsers() {
		List<User> usersFromDB = userRepository.findAll().stream()
				.filter(user -> user.getAuthorities().contains(Authorities.USER)).toList();
		if (usersFromDB.isEmpty())
			return (ResponseEntity<?>) ResponseEntity.status(HttpStatus.NO_CONTENT);
		List<UserShowDTO> usersToResponse = new ArrayList<UserShowDTO>();
		for (User user : usersFromDB) {
			usersToResponse
					.add(new UserShowDTO(user.getSurname(), user.getName(), user.getLastname(), user.getEmail()));
		}
		return ResponseEntity.status(HttpStatus.OK).body(usersToResponse);
	}

}
