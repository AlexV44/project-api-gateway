package com.project.api_gateway.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record RegisterRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8) String password,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull @Past LocalDate birthday
) {
}
