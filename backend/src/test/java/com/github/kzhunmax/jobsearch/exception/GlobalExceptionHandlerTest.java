package com.github.kzhunmax.jobsearch.exception;

import com.github.kzhunmax.jobsearch.payload.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    private static final String TEST_REQUEST_ID = "test-request-id";
    private static final String REQUEST_ID_MDC_KEY = "requestId";

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        MDC.put(REQUEST_ID_MDC_KEY, TEST_REQUEST_ID);
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException and return BAD_REQUEST response")
    void handleIllegalArgumentException_shouldReturnBadRequest() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid input data");

        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleIllegalArgumentException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errors().getFirst().code()).isEqualTo("INVALID_DATA");
        assertThat(response.getBody().errors().getFirst().message()).isEqualTo("Invalid input data");
        assertThat(response.getBody().requestId()).isEqualTo(TEST_REQUEST_ID);
    }

    @Test
    @DisplayName("Should handle AuthenticationException and return UNAUTHORIZED response")
    void handleAuthenticationException_shouldReturnUnauthorized() {
        AuthenticationException ex = mock(AuthenticationException.class);

        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleAuthenticationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errors().getFirst().code()).isEqualTo("AUTH_FAILED");
        assertThat(response.getBody().errors().getFirst().message()).isEqualTo("Authentication is required");
        assertThat(response.getBody().requestId()).isEqualTo(TEST_REQUEST_ID);
    }

    @Test
    @DisplayName("Should handle ApiException and return custom status response")
    void handleApiException_shouldReturnCustomStatus() {
        HttpStatus customStatus = HttpStatus.NOT_FOUND;
        String errorCode = "NOT_FOUND";
        String message = "Resource not found";
        ApiException ex = new ApiException(message, customStatus, errorCode);

        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleApiException(ex);

        assertThat(response.getStatusCode()).isEqualTo(customStatus);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errors().getFirst().code()).isEqualTo(errorCode);
        assertThat(response.getBody().errors().getFirst().message()).isEqualTo(message);
        assertThat(response.getBody().requestId()).isEqualTo(TEST_REQUEST_ID);
    }
}
