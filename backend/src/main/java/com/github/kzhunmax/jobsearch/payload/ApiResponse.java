package com.github.kzhunmax.jobsearch.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Schema(description = "Standard API response wrapper with consistent structure")
public record ApiResponse<T>(

        @Schema(description = "The actual response payload", nullable = true)
        T data,

        @Schema(description = "List actual response payload", nullable = true)
        List<ErrorDetails> errors,

        @Schema(description = "Server-side timestamp of response creation")
        Instant timestamp,

        @Schema(description = "Error details for failed requests")
        String requestId
) {
    @Schema(description = "Error details structure")
    public record ErrorDetails(

            @Schema(description = "Machine-readable error code")
            String code,

            @Schema(description = "Human-readable error message")
            String message
    ) {}

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