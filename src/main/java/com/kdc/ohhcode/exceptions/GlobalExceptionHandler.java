package com.kdc.ohhcode.exceptions;

import com.kdc.ohhcode.dtos.error.ApiErrorDto;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.AuthenticationException;
import java.time.LocalDate;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorDto> handleAccessDenied(AccessDeniedException e,
                                                          HttpServletRequest request) {
        ApiErrorDto apiErrorDto = new ApiErrorDto(
                LocalDate.now(),
                HttpStatus.FORBIDDEN.value(),
                e.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(
                apiErrorDto,
                HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorDto> handleDataIntegrityViolationException(DataIntegrityViolationException e,
                                                                             HttpServletRequest request) {
        ApiErrorDto apiErrorDto = new ApiErrorDto(
                LocalDate.now(),
                HttpStatus.CONFLICT.value(),
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(apiErrorDto.statusCode())
                .body(apiErrorDto);
    }

    @ExceptionHandler(value = AuthenticationException.class)
    public ResponseEntity<ApiErrorDto> handleAuthenticationException(AuthenticationException e,
                                                                     HttpServletRequest request) {

        ApiErrorDto apiErrorDto = new ApiErrorDto(
                LocalDate.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid username or password : " + e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(apiErrorDto.statusCode())
                .body(apiErrorDto);
    }


    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorDto> handleBadCredentialsException(BadCredentialsException e,
                                                                     HttpServletRequest request) {
        ApiErrorDto apiErrorDto = new ApiErrorDto(
                LocalDate.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Authentication Failed : Invalid username or password",
                request.getRequestURI()
        );
        return ResponseEntity.status(apiErrorDto.statusCode())
                .body(apiErrorDto);
    }


    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiErrorDto> handleJwtException(JwtException e,
                                                          HttpServletRequest request) {
        ApiErrorDto apiErrorDto = new ApiErrorDto(
                LocalDate.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid JWT Token :" + e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(apiErrorDto.statusCode())
                .body(apiErrorDto);
    }




    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException e,
                                                                             HttpServletRequest request) {
        String msg = e.getBindingResult()
                .getAllErrors()
                .getFirst()
                .getDefaultMessage();

        ApiErrorDto apiErrorDto = new ApiErrorDto(
                LocalDate.now(),
                HttpStatus.BAD_REQUEST.value(),
                msg,
                request.getRequestURI()
        );
        return ResponseEntity.status(apiErrorDto.statusCode())
                .body(apiErrorDto);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorDto> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        ApiErrorDto apiErrorDto = new ApiErrorDto(
                LocalDate.now(),
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(apiErrorDto.statusCode())
                .body(apiErrorDto);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDto> handleException(Exception e,
                                                       HttpServletRequest request) {
        ApiErrorDto apiErrorDto = new ApiErrorDto(
                LocalDate.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(apiErrorDto.statusCode())
                .body(apiErrorDto);
    }
}
