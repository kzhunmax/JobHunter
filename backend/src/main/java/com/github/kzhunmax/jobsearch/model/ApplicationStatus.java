package com.github.kzhunmax.jobsearch.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Job application statuses")
public enum ApplicationStatus {

    @Schema(description = "Application submitted")
    APPLIED,

    @Schema(description = "Application under review")
    UNDER_REVIEW,

    @Schema(description = "Interview conducted")
    INTERVIEWED,

    @Schema(description = "Application rejected")
    REJECTED,

    @Schema(description = "Offer proposed")
    OFFERED,

    @Schema(description = "Offer accepted")
    ACCEPTED
}
