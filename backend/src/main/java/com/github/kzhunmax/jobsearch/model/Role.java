package com.github.kzhunmax.jobsearch.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@Schema(description = "User roles in job search platform")
public enum Role {

    @Schema(
            description = "Candidate - user, that search for job",
            example = "ROLE_CANDIDATE"
    )
    ROLE_CANDIDATE,

    @Schema(
            description = "Recruiter - user, which posts job vacancies",
            example = "ROLE_RECRUITER"
    )
    ROLE_RECRUITER,

    @Schema(
            description = "Admin - user, that has full control of platform",
            example = "ROLE_ADMIN"
    )
    ROLE_ADMIN;

}
