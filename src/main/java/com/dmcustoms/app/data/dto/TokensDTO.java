package com.dmcustoms.app.data.dto;

public record TokensDTO(String accessToken, String accessTokenExpiredAt, String refreshToken, String refreshTokenExpiredAt) {
}
