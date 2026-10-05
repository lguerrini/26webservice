package com.example.webservice.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 45) String firstname,
        @NotBlank @Size(max = 45) String lastname,
        @NotBlank @Size(max = 45) String username,
        @NotBlank @Size(min = 8, max = 72) String password,
        @Email @Size(max = 45) String email
) {
}