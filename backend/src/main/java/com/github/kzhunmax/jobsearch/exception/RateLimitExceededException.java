package com.github.kzhunmax.jobsearch.exception;

import org.springframework.http.HttpStatus;

public class RateLimitExceededException extends ApiException {
    public RateLimitExceededException() {
        super("Too many requests, please try again later.", HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED");
    }
}
