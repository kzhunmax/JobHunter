package com.github.kzhunmax.jobsearch.exception;

import com.github.kzhunmax.jobsearch.payload.ApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("IllegalArgumentException caught - {}", ex.getMessage());
        return ApiResponse.error(HttpStatus.BAD_REQUEST, "INVALID_DATA", ex.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("AuthenticationException caught - {}", ex.getMessage());
        return ApiResponse.error(HttpStatus.UNAUTHORIZED, "AUTH_FAILED", "Authentication is required");
    }

    @ExceptionHandler(OAuth2AuthenticationProcessingException.class)
    public ResponseEntity<ApiResponse<Object>> handleOAuth2AuthenticationProcessingException(OAuth2AuthenticationProcessingException ex) {
        log.warn("OAuth2AuthenticationProcessingException caught - {}", ex.getMessage());
        return ApiResponse.error(HttpStatus.UNAUTHORIZED, "AUTH_FAILED", ex.getMessage());
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Object>> handleApiException(ApiException ex) {
        log.warn("ApiException - {} | status {} | code {}", ex.getMessage(), ex.getHttpStatus(), ex.getErrorCode());
        return ApiResponse.error(ex.getHttpStatus(), ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.warn("MethodArgumentNotValidException caught - {}", ex.getMessage());
        return ApiResponse.error(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("BadCredentialsException caught - {}", ex.getMessage());
        return ApiResponse.error(HttpStatus.UNAUTHORIZED, "AUTH_FAILED", "Invalid username or password");
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleExpiredJwt(ExpiredJwtException ex) {
        log.warn("ExpiredJwtException caught - {}", ex.getMessage());
        return ApiResponse.error(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "JWT token has expired");
    }

    @ExceptionHandler({MalformedJwtException.class, SignatureException.class})
    public ResponseEntity<ApiResponse<Void>> handleInvalidJwt(RuntimeException ex) {
        log.warn("InvalidJwtException caught - {}", ex.getMessage());
        return ApiResponse.error(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "JWT token is invalid");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied - {}", ex.getMessage());
        return ApiResponse.error(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "You do not have permission to perform this action");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String paramName = ex.getName();
        String requiredType = ex.getRequiredType() != null
                ? ex.getRequiredType().getSimpleName()
                : "unknown";

        String message = String.format("Invalid value for parameter '%s'. Expected a %s.", paramName, requiredType);

        log.warn("Type mismatch for parameter '{}' - expected={}, received='{}' ", paramName, requiredType, ex.getValue(), ex);
        return ApiResponse.error(HttpStatus.BAD_REQUEST, "TYPE_MISMATCH", message);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<Object>> handleDisabledException(DisabledException ex) {
        log.warn("Authentication failed for disabled account - {}", ex.getMessage());
        return ApiResponse.error(HttpStatus.FORBIDDEN, "EMAIL_NOT_VERIFIED", "Please verify your email address before logging in.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleUnexpected(Exception ex) {
        log.error("Unexpected error occurred - {}", ex.getMessage(), ex);
        return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected error");
    }
}
