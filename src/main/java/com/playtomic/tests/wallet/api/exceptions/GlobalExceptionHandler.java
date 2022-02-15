package com.playtomic.tests.wallet.api.exceptions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.playtomic.tests.wallet.service.StripeAmountTooSmallException;
import com.playtomic.tests.wallet.service.StripeServiceException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StripeAmountTooSmallException.class)
    public ResponseEntity<ErrorResponse> stripeAmountTooSmallException(StripeAmountTooSmallException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Amount is less then minimal", 400, Instant.now()));
    }

    @ExceptionHandler(StripeServiceException.class)
    public ResponseEntity<ErrorResponse> stripeServiceException(StripeServiceException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Sorry, we have some problems :)", 500, Instant.now()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> entityNotFoundException(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage(), 404, Instant.now()));
    }

    @AllArgsConstructor
    private static class ErrorResponse {

        @JsonProperty("message")
        String message;

        @JsonProperty("status")
        int status;

        @JsonProperty("timestamp")
        Instant timestamp;
    }
}
