package com.github.kzhunmax.jobsearch.payload;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Collections;
import java.util.List;


public record ApiResponse<T>(
        T data,
        List<ErrorDetails> errors,
        Instant timestamp,
        String requestId
) {
    public record ErrorDetails(String code, String message) {}

    public static <T> ResponseEntity<ApiResponse<T>> of(HttpStatus status, T data, List<ErrorDetails> errors, String requestId) {
        return ResponseEntity
                .status(status)
                .body(new ApiResponse<>(data, errors != null ? errors : Collections.emptyList(), Instant.now(), requestId));
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(T data, String requestId) {
        return of(HttpStatus.OK, data, null, requestId);
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String requestId) {
        return of(HttpStatus.CREATED, data, null, requestId);
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, String errorCode, String errorMessage, String requestId) {
        return of(status, null,  List.of(new ErrorDetails(errorCode, errorMessage)), requestId);
    }

    public static <T> ResponseEntity<ApiResponse<T>> noContent(String requestId) {
        return of(HttpStatus.NO_CONTENT, null,  null,  requestId);
    }
}
