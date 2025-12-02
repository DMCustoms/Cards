package com.dmcustoms.app.data.dto;

public record TokenDTO(String accessToken, String accessTokenExpiredAt, String refreshToken, String refreshTokenExpiredAt) {
}
