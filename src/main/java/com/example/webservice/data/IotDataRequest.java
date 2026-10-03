package com.example.webservice.data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IotDataRequest(
        @NotNull Integer idIot,
        @Size(max = 120) String jsondata
) {
}