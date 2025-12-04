package com.dmcustoms.app.jwt.core;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.function.Function;

import com.dmcustoms.app.data.types.JwtAuthorities;

public class AccessTokenFactory implements Function<Token, Token> {

	private final Duration ttl = Duration.ofMinutes(5);

	@Override
	public Token apply(Token token) {
		Instant now = Instant.now();
		return new Token(token.getId(), token.getSubject(), Arrays.asList(JwtAuthorities.JWT_ACCESS),
				now, now.plus(this.ttl));
	}

}
