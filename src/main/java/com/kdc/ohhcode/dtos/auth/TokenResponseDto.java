package com.kdc.ohhcode.dtos.auth;

import com.kdc.ohhcode.entities.enums.Role;

import java.util.Date;

public record TokenResponseDto(
        String token,
        String refreshToken,
        Role role,
        Date issuedAt,
        Date expiration
) {
}
