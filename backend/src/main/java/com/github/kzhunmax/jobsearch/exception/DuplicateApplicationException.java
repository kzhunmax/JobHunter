package com.github.kzhunmax.jobsearch.exception;

import org.springframework.http.HttpStatus;

public class DuplicateApplicationException extends ApiException {
  public DuplicateApplicationException() {
    super("User has already applied to this job",
            HttpStatus.CONFLICT, "DUPLICATE_APPLICATION");
  }
}