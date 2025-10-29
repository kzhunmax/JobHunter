package com.github.kzhunmax.jobsearch.exception;

import org.springframework.http.HttpStatus;

public class ResumeOwnershipException extends ApiException {
    public ResumeOwnershipException() {
        super("Access denied: You do not own this resume.", HttpStatus.FORBIDDEN, "ACCESS_DENIED");
    }
}
