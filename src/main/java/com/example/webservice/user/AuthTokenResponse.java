package com.example.webservice.user;

import java.time.Instant;

public record AuthTokenResponse(String accessToken, String tokenType, Instant expiresAt) {
}