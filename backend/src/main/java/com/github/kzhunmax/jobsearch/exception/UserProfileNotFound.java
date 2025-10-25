package com.github.kzhunmax.jobsearch.exception;

import org.springframework.http.HttpStatus;

public class UserProfileNotFound extends ApiException {
    public UserProfileNotFound(Long userProfileId) {
        super("User profile with id " + userProfileId + " not found",
                HttpStatus.NOT_FOUND, "USER_PROFILE_NOT_FOUND");
    }
}
