package com.github.kzhunmax.jobsearch.model;

import lombok.Getter;

@Getter
public enum Language {
    ENGLISH("English"),
    SPANISH("Spanish"),
    FRENCH("French"),
    GERMAN("German"),
    ITALIAN("Italian"),
    PORTUGUESE("Portuguese"),
    RUSSIAN("Russian"),
    CHINESE("Chinese"),
    JAPANESE("Japanese"),
    KOREAN("Korean"),
    ARABIC("Arabic"),
    HINDI("Hindi"),
    BENGALI("Bengali"),
    URDU("Urdu"),
    TURKISH("Turkish"),
    DUTCH("Dutch"),
    SWEDISH("Swedish"),
    POLISH("Polish"),
    VIETNAMESE("Vietnamese"),
    GREEK("Greek"),
    CZECH("Czech"),
    ROMANIAN("Romanian"),
    HUNGARIAN("Hungarian"),
    DANISH("Danish"),
    FINNISH("Finnish"),
    NORWEGIAN("Norwegian"),
    HEBREW("Hebrew"),
    THAI("Thai"),
    MALAY("Malay"),
    INDONESIAN("Indonesian"),
    FILIPINO("Filipino"),
    UKRAINIAN("Ukrainian");

    private final String displayName;

    Language(String displayName) {
        this.displayName = displayName;
    }
}
