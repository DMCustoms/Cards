package com.dmcustoms.app.jwt.core;

import java.util.function.Function;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class JwtAuthenticationConverter implements AuthenticationConverter {

	private final Function<String, Token> accessTokenStringDeserializer;

	private final Function<String, Token> refreshTokenStringDeserializer;

	@Override
	public @Nullable Authentication convert(HttpServletRequest request) {
		String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authorization != null && authorization.startsWith("Bearer ")) {
			String token = authorization.replace("Bearer ", "");
			Token accessToken = this.accessTokenStringDeserializer.apply(token);
			if (accessToken != null)
				return new PreAuthenticatedAuthenticationToken(accessToken, token);
			Token refreshToken = this.refreshTokenStringDeserializer.apply(token);
			if (refreshToken != null)
				return new PreAuthenticatedAuthenticationToken(refreshToken, token);
		}
		return null;
	}

}
