package com.dmcustoms.app.data.entities;

import java.util.Date;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity(name = "deactivated_token")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE, force = true)
public class DeactivatedToken {

	@Id
	private final UUID id;
	
	@Column(name = "keep_until")
	private final Date keepUntil;
	
}
