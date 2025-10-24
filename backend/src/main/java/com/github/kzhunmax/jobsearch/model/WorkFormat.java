package com.github.kzhunmax.jobsearch.model;

import lombok.Getter;

@Getter
public enum WorkFormat {

    REMOTE("Remote"),
    OFFICE("Office"),
    HYBRID("Hybrid");

    private final String displayName;

    WorkFormat(String displayName) {
        this.displayName = displayName;
    }
}
