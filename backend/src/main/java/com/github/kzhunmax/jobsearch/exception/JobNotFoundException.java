package com.github.kzhunmax.jobsearch.exception;

import org.springframework.http.HttpStatus;

public class JobNotFoundException extends ApiException {
  public JobNotFoundException(Long jobId) {
    super("Job with id " + jobId + " not found",
            HttpStatus.NOT_FOUND, "JOB_NOT_FOUND");
  }
}