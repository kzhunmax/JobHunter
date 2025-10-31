package com.github.kzhunmax.jobsearch.exception;

import org.springframework.http.HttpStatus;

public class CompanyNotFoundException extends ApiException {
    public CompanyNotFoundException(Long companyId) {
        super("Company with id " + companyId + " not found", HttpStatus.NOT_FOUND, "COMPANY_NOT_FOUND");
    }
}
