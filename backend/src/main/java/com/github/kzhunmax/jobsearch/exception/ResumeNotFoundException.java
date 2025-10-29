package com.github.kzhunmax.jobsearch.exception;

import org.springframework.http.HttpStatus;

public class ResumeNotFoundException extends ApiException {
    public ResumeNotFoundException(Long userProfileId) {
        super("Resume with id " + userProfileId + " not found", HttpStatus.NOT_FOUND, "RESUME_NOT_FOUND");
    }
}
