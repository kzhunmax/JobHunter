package com.github.kzhunmax.jobsearch.exception;

import org.springframework.http.HttpStatus;

public class IncompleteProfileException extends ApiException {
    public IncompleteProfileException() {
        super("Your profile is incomplete. Please update it before applying.", HttpStatus.BAD_REQUEST, "PROFILE_INCOMPLETE");
    }
}
