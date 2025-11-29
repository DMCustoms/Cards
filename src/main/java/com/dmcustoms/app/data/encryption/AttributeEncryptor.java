package com.dmcustoms.app.data.encryption;

import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Converter
@Slf4j
public class AttributeEncryptor implements AttributeConverter<String, byte[]>{

private final SecretKeySpec key;
	
	public AttributeEncryptor(@Value("${aes.encryption.secret}") String secret) {
		key = new SecretKeySpec(secret.getBytes(), "AES");
	}
	
	@Override
	public byte[] convertToDatabaseColumn(String attribute) {
		try {
			Cipher encryptor = Cipher.getInstance("AES");
			encryptor.init(Cipher.ENCRYPT_MODE, key);
			byte[] bytes = encryptor.doFinal(attribute.getBytes());
			return bytes;
		} catch (GeneralSecurityException e) {
			log.error(e.getMessage());
			return new byte[0];
		}
	}

	@Override
	public String convertToEntityAttribute(byte[] dbData) {
		try {
			Cipher decryptor = Cipher.getInstance("AES");
			decryptor.init(Cipher.DECRYPT_MODE, key);
			byte[] bytes = decryptor.doFinal(dbData);
			String result = "";
			for (byte symbol : bytes) {
				result += (char) symbol;
			}
			return result;
		} catch (GeneralSecurityException e) {
			log.error(e.getMessage());
			return "";
		}
	}

}
