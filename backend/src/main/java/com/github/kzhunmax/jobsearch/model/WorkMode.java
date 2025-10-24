package com.github.kzhunmax.jobsearch.model;

import lombok.Getter;

@Getter
public enum WorkMode {
    FULL_TIME("Full-time"),
    PART_TIME("Part-time");

    private final String displayName;

    WorkMode(String displayName) {
        this.displayName = displayName;
    }
}
