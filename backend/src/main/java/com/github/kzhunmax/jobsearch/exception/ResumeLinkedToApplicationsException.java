package com.github.kzhunmax.jobsearch.exception;

import org.springframework.http.HttpStatus;

public class ResumeLinkedToApplicationsException extends ApiException {
    public ResumeLinkedToApplicationsException() {
        super("Cannot delete resume as it is linked to existing job applications.", HttpStatus.CONFLICT, "RESUME_IN_USE");
    }
}
