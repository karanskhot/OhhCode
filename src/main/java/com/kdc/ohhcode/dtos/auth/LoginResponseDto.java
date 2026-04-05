package com.kdc.ohhcode.dtos.auth;

public record LoginResponseDto(
        String username,
        TokenResponseDto authData
) {
}
