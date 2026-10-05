package com.example.webservice.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @NotBlank @Size(max = 45) String firstname,
        @NotBlank @Size(max = 45) String lastname,
        @NotBlank @Size(max = 45) String username,
        @Size(max = 72) String password,
        @Email @Size(max = 45) String email,
        @NotBlank @Size(max = 45) String role,
        @Size(max = 45) String tUsercol
) {
}