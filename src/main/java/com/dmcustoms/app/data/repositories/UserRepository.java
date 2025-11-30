package com.dmcustoms.app.data.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dmcustoms.app.data.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {
	
	public Optional<User> findUserByEmail(String email);
	
}
