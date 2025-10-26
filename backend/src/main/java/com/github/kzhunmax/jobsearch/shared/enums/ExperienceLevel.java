package com.github.kzhunmax.jobsearch.shared.enums;

import lombok.Getter;

@Getter
public enum ExperienceLevel {
    NO_EXPERIENCE("No experience"),
    HALF_YEAR("0.5 years"),
    ONE_YEAR("1 year"),
    TWO_YEARS("2 years"),
    THREE_YEARS("3 years"),
    FOUR_YEARS("4 years"),
    FIVE_YEARS("5 years"),
    SIX_YEARS("6 years"),
    SEVEN_YEARS("7 years"),
    EIGHT_YEARS("8 years"),
    NINE_YEARS("9 years"),
    TEN_YEARS("10 years"),
    MORE_THAN_TEN_YEARS("More than 10 years");

    private final String displayName;

    ExperienceLevel(String displayName) {
        this.displayName = displayName;
    }
}