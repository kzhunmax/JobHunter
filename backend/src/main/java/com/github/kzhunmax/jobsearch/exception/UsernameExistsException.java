package com.github.kzhunmax.jobsearch.exception;

import org.springframework.http.HttpStatus;

public class UsernameExistsException extends ApiException {
    public UsernameExistsException(String username) {
        super("Username " + username + " is already taken",
                HttpStatus.BAD_REQUEST,
                "USERNAME_TAKEN");
    }
}
