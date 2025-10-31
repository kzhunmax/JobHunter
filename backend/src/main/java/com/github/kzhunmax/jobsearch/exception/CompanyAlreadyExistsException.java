package com.github.kzhunmax.jobsearch.exception;

import org.springframework.http.HttpStatus;

public class CompanyAlreadyExistsException extends ApiException {
    public CompanyAlreadyExistsException(String name) {
        super("Company with name '" + name + "' already exists", HttpStatus.CONFLICT, "COMPANY_NAME_TAKEN");
    }
}
