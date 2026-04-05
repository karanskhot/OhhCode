package com.kdc.ohhcode.dtos.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
        @NotBlank(message = "username is required")
        @Email(message = "invalid email format")
        String username,

        @NotBlank(message = "password is required")
        String password
) {
}
