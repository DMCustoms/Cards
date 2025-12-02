package com.dmcustoms.app.jwt.core;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;

public class AccessTokenFactory implements Function<Token, Token> {

	private final Duration ttl = Duration.ofMinutes(5);

	@Override
	public Token apply(Token token) {
		Instant now = Instant.now();
		return new Token(token.getId(), token.getSubject(),
				token.getAuthorities().stream().filter(authority -> authority.startsWith("GRANT_"))
						.map(authority -> authority.replace("GRANT_", "")).toList(),
				now, now.plus(this.ttl));
	}

}
