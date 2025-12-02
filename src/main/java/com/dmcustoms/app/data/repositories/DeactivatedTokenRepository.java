package com.dmcustoms.app.data.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dmcustoms.app.data.entities.DeactivatedToken;

public interface DeactivatedTokenRepository extends JpaRepository<DeactivatedToken, UUID> {
}
