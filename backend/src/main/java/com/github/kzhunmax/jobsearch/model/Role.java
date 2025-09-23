package com.github.kzhunmax.jobsearch.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Schema(description = "User roles in job search platform")
public enum Role {

    @Schema(
            description = "Candidate - user, that search for job",
            example = "ROLE_USER"
    )
    ROLE_USER("U"),

    @Schema(
            description = "Recruiter - user, which posts job vacancies",
            example = "ROLE_RECRUITER"
    )
    ROLE_RECRUITER("R"),

    @Schema(
            description = "Admin - user, that has full control of platform",
            example = "ROLE_ADMIN"
    )
    ROLE_ADMIN("A");

    private final String code;
}
