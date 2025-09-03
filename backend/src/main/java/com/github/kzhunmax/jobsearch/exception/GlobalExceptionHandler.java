package com.github.kzhunmax.jobsearch.exception;

import com.github.kzhunmax.jobsearch.payload.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String REQUEST_ID_MDC_KEY = "requestId";

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.warn("IllegalArgumentException caught: {} | requestId={}", ex.getMessage(), requestId);
        return ApiResponse.error(HttpStatus.BAD_REQUEST, "INVALID_DATA", ex.getMessage(), requestId);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(AuthenticationException ex) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.warn("AuthenticationException caught: {} | requestId={}", ex.getMessage(), requestId);
        return ApiResponse.error(HttpStatus.UNAUTHORIZED, "AUTH_FAILED", "Invalid username or password", requestId
        );
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Object>> handleApiException(ApiException ex) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.warn("ApiException: {} | status {} | code {} | requestId={}",
                ex.getMessage(), ex.getHttpStatus(), ex.getErrorCode(), requestId);
        return ApiResponse.error(ex.getHttpStatus(), ex.getErrorCode(), ex.getMessage(), requestId);
    }
}
