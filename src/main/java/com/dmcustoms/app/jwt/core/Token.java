package com.dmcustoms.app.jwt.core;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.dmcustoms.app.data.types.JwtAuthorities;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Token {
	
	private final UUID id;
	
	private final String subject;
	
	private final List<JwtAuthorities> authorities;
	
	private final Instant createdAt;
	
	private final Instant expiresAt;

}
