package com.dmcustoms.app.jwt.serializers;

import java.util.Date;
import java.util.function.Function;

import com.dmcustoms.app.jwt.core.Token;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@RequiredArgsConstructor
public class AccessTokenSerializer implements Function<Token, String> {

	private final JWSSigner jwsSigner;

	private final JWSAlgorithm jwsAlgorithm = JWSAlgorithm.HS256;

	@Override
	public String apply(Token token) {
		JWSHeader jwsHeader = new JWSHeader.Builder(this.jwsAlgorithm).keyID(token.getId().toString()).build();
		JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().jwtID(token.getId().toString())
				.subject(token.getSubject()).issueTime(Date.from(token.getCreatedAt()))
				.expirationTime(Date.from(token.getExpiresAt())).claim("authorities", token.getAuthorities()).build();
		SignedJWT signedJWT = new SignedJWT(jwsHeader, jwtClaimsSet);
		try {
			signedJWT.sign(jwsSigner);
			return signedJWT.serialize();
		} catch (JOSEException e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

}
