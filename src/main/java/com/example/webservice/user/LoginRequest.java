package com.example.webservice.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank @Size(max = 45) String username,
        @NotBlank @Size(max = 72) String password
) {
}