package com.github.kzhunmax.jobsearch.user.dto;

import com.github.kzhunmax.jobsearch.shared.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "User data response object containing basic user information")
public record UserResponseDTO(

        @Schema(description = "User's email address")
        String email,

        @Schema(description = "Set of user roles defining access permissions")
        Set<Role> roles
) {
}

