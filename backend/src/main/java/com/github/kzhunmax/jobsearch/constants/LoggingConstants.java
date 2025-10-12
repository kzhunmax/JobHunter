package com.github.kzhunmax.jobsearch.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LoggingConstants {
    public static final String REQUEST_ID_MDC_KEY = "requestId";
    public static final String REQUEST_ID_HEADER = "X-Request-ID";
}
