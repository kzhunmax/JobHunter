package com.github.kzhunmax.jobsearch.exception;

import org.springframework.http.HttpStatus;

public class InvalidOrExpiredTokenException extends ApiException {
    public InvalidOrExpiredTokenException() {
        super("Refresh token is invalid or expired", HttpStatus.UNAUTHORIZED, "INVALID_REFRESH");
    }
}
