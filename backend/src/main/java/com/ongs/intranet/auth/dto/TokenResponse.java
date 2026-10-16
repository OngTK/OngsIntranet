package com.ongs.intranet.auth.dto;

public record TokenResponse(String accessToken, long expiresIn) {
}
