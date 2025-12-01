package com.github.kzhunmax.jobsearch.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;

@AllArgsConstructor
@Getter
public enum PricingPlan {
    FREE(10, Duration.ofMinutes(1)),
    PREMIUM(100, Duration.ofMinutes(1));

    private final int capacity;
    private final Duration duration;
}
