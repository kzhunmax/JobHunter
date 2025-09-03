package com.github.kzhunmax.jobsearch.dto.request;

public record UserLoginDTO(
        String usernameOrEmail,
        String password
) {
}
