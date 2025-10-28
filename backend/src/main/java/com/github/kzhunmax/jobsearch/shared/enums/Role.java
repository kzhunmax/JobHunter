package com.github.kzhunmax.jobsearch.shared.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "User roles in job search platform")
public enum Role {

    @Schema(description = "Candidate - user, that search for job")
    ROLE_CANDIDATE,

    @Schema(description = "Recruiter - user, which posts job vacancies")
    ROLE_RECRUITER,

    @Schema(description = "Admin - user, that has full control of platform")
    ROLE_ADMIN

}
