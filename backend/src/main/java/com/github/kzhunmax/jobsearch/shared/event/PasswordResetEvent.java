package com.github.kzhunmax.jobsearch.shared.event;

public record PasswordResetEvent(
        String email,
        String token
) {
}
