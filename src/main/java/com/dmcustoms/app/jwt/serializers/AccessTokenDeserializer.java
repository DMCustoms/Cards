package com.dmcustoms.app.jwt.serializers;

import java.text.ParseException;
import java.util.UUID;
import java.util.function.Function;

import com.dmcustoms.app.jwt.core.Token;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@RequiredArgsConstructor
public class AccessTokenDeserializer implements Function<String, Token> {

	private final JWSVerifier jwsVerifier;

	@Override
	public Token apply(String token) {
		try {
			SignedJWT signedJWT = SignedJWT.parse(token);
			if (signedJWT.verify(jwsVerifier)) {
				JWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();
				return new Token(UUID.fromString(jwtClaimsSet.getJWTID()), jwtClaimsSet.getSubject(),
						jwtClaimsSet.getStringListClaim("authorities"), jwtClaimsSet.getIssueTime().toInstant(),
						jwtClaimsSet.getExpirationTime().toInstant());
			}
		} catch (ParseException | JOSEException e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

}
