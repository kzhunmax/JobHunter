package com.github.kzhunmax.jobsearch.exception;

import com.github.kzhunmax.jobsearch.payload.ApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static com.github.kzhunmax.jobsearch.constants.LoggingConstants.REQUEST_ID_MDC_KEY;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.warn("Request [{}]: IllegalArgumentException caught - {}", requestId, ex.getMessage());
        return ApiResponse.error(HttpStatus.BAD_REQUEST, "INVALID_DATA", ex.getMessage(), requestId);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(AuthenticationException ex) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.warn("Request [{}]: AuthenticationException caught - {}", requestId, ex.getMessage());
        return ApiResponse.error(HttpStatus.UNAUTHORIZED, "AUTH_FAILED", "Authentication is required", requestId);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Object>> handleApiException(ApiException ex) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.warn("Request [{}]: ApiException - {} | status {} | code {}",
                requestId, ex.getMessage(), ex.getHttpStatus(), ex.getErrorCode());
        return ApiResponse.error(ex.getHttpStatus(), ex.getErrorCode(), ex.getMessage(), requestId);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.warn("Request [{}]: MethodArgumentNotValidException caught - {}", requestId, ex.getMessage());
        return ApiResponse.error(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed", requestId);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.warn("Request [{}]: BadCredentialsException caught - {}", requestId, ex.getMessage());
        return ApiResponse.error(HttpStatus.UNAUTHORIZED, "AUTH_FAILED", "Invalid username or password", requestId);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleExpiredJwt(ExpiredJwtException ex) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.warn("Request [{}]: ExpiredJwtException caught - {}", requestId, ex.getMessage());
        return ApiResponse.error(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "JWT token has expired", requestId);
    }

    @ExceptionHandler({MalformedJwtException.class, SignatureException.class})
    public ResponseEntity<ApiResponse<Void>> handleInvalidJwt(RuntimeException ex) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.warn("Request [{}]: InvalidJwtException caught - {}", requestId, ex.getMessage());
        return ApiResponse.error(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "JWT token is invalid", requestId);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException ex) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.warn("Request [{}]: Access denied - {}",  requestId, ex.getMessage());
        return ApiResponse.error(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "You do not have permission to perform this action", requestId);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        String paramName = ex.getName();
        String requiredType = ex.getRequiredType() != null
                ? ex.getRequiredType().getSimpleName()
                : "unknown";

        String message = String.format("Invalid value for parameter '%s'. Expected a %s.", paramName, requiredType);

        log.warn("Request [{}]: Type mismatch for parameter '{}' - expected={}, received='{}' ",  requestId, paramName, requiredType, ex.getValue(), ex);
        return ApiResponse.error(HttpStatus.BAD_REQUEST, "TYPE_MISMATCH", message, requestId);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleUnexpected(Exception ex) {
        log.error("Request [{}]: Unexpected error occurred - {}", MDC.get(REQUEST_ID_MDC_KEY), ex.getMessage(), ex);
        return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected error", MDC.get(REQUEST_ID_MDC_KEY));
    }
}
