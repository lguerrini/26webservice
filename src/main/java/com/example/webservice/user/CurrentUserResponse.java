package com.example.webservice.user;

public record CurrentUserResponse(String username, String role, String accessToken) {
}