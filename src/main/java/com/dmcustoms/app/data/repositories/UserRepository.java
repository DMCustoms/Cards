package com.dmcustoms.app.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dmcustoms.app.data.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
