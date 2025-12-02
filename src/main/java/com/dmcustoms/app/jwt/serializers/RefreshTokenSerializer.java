package com.dmcustoms.app.jwt.serializers;

import java.util.Date;
import java.util.function.Function;

import com.dmcustoms.app.jwt.core.Token;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@RequiredArgsConstructor
public class RefreshTokenSerializer implements Function<Token, String> {

	private final JWEEncrypter jweEncrypter;

	private final JWEAlgorithm jweAlgorithm = JWEAlgorithm.DIR;

	private final EncryptionMethod encryptionMethod = EncryptionMethod.A128GCM;

	@Override
	public String apply(Token token) {
		JWEHeader jweHeader = new JWEHeader.Builder(this.jweAlgorithm, this.encryptionMethod)
				.keyID(token.getId().toString()).build();
		JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().jwtID(token.getId().toString())
				.subject(token.getSubject()).issueTime(Date.from(token.getCreatedAt()))
				.expirationTime(Date.from(token.getExpiresAt())).claim("authorities", token.getAuthorities()).build();
		EncryptedJWT encryptedJWT = new EncryptedJWT(jweHeader, jwtClaimsSet);
		try {
			encryptedJWT.encrypt(jweEncrypter);
			return encryptedJWT.serialize();
		} catch (JOSEException e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

}
