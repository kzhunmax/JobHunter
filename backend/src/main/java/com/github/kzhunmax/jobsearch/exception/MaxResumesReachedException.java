package com.github.kzhunmax.jobsearch.exception;

import org.springframework.http.HttpStatus;

public class MaxResumesReachedException extends ApiException {
    public MaxResumesReachedException(int limitResumesPerUser) {
        super("Maximum number of resumes (" + limitResumesPerUser + ") reached.", HttpStatus.BAD_REQUEST, "MAX_RESUMES_REACHED");
    }
}
