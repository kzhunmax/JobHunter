package com.github.kzhunmax.jobsearch.exception;

import org.springframework.http.HttpStatus;

public class UserProfileNotFound extends ApiException {
    public UserProfileNotFound(Long userId) {
        super("User profile with id " + userId + " not found", HttpStatus.NOT_FOUND, "USER_PROFILE_NOT_FOUND");
    }
}
