package com.kdc.ohhcode.dtos.error;

import java.time.LocalDate;

public record ApiErrorDto(
        LocalDate timestamp,
        int statusCode,
        Object error,
        String path
) {
}

