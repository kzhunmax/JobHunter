package com.github.kzhunmax.jobsearch.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends ApiException {
  public UserNotFoundException(String username) {
    super("User " + username + " not found",
            HttpStatus.NOT_FOUND, "USER_NOT_FOUND");
  }
}