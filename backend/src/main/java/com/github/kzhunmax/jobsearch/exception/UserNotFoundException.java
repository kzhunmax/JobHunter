package com.github.kzhunmax.jobsearch.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends ApiException {
    public UserNotFoundException(Long userId) {
        super("User with id " + userId + " not found", HttpStatus.NOT_FOUND, "USER_NOT_FOUND");
    }
}
