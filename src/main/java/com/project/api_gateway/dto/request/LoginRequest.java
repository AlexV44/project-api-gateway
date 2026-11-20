package com.project.api_gateway.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank
        String login,
        @NotBlank
        String password
) {
}
