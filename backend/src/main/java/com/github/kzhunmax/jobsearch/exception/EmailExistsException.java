package com.github.kzhunmax.jobsearch.exception;

import org.springframework.http.HttpStatus;

public class EmailExistsException extends ApiException {
    public EmailExistsException(String email) {
        super("Email " + email + " is already taken",
                HttpStatus.BAD_REQUEST,
                "EMAIL_TAKEN");
    }
}
