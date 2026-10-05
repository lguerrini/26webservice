package com.example.webservice.user;

public record UserResponse(
        Integer id,
        String firstname,
        String lastname,
        String username,
        String email,
        String role,
        String tUsercol
) {
}