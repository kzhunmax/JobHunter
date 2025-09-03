package com.github.kzhunmax.jobsearch.exception;

import org.springframework.http.HttpStatus;

public class ApplicationNotFoundException extends ApiException {
  public ApplicationNotFoundException(Long appId) {
    super("Application with id " + appId + " not found",
            HttpStatus.NOT_FOUND, "APPLICATION_NOT_FOUND");
  }
}