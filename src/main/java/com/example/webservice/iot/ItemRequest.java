package com.example.webservice.iot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ItemRequest(
        @NotBlank @Size(max = 45) String name,
        @Size(max = 245) String description,
        @NotBlank @Size(max = 45) String macaddress,
        @Size(max = 512) String jsonrange
) {
}