package com.github.kzhunmax.jobsearch.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Role {
    ROLE_USER("U"), ROLE_RECRUITER("R"), ROLE_ADMIN("A");

    private final String code;
}
