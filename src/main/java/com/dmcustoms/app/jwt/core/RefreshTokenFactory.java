package com.dmcustoms.app.jwt.core;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.UUID;
import java.util.function.Function;

import org.springframework.security.core.Authentication;

import com.dmcustoms.app.data.types.JwtAuthorities;

public class RefreshTokenFactory implements Function<Authentication, Token> {

	private final Duration ttl = Duration.ofDays(1);

	@Override
	public Token apply(Authentication authentication) {
		LinkedList<JwtAuthorities> authorities = new LinkedList<JwtAuthorities>();
		authorities.add(JwtAuthorities.JWT_REFRESH);
		authorities.add(JwtAuthorities.JWT_LOGOUT);
		Instant now = Instant.now();
		return new Token(UUID.randomUUID(), authentication.getName(), authorities, now, now.plus(this.ttl));
	}

}
