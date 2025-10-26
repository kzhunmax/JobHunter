package com.github.kzhunmax.jobsearch.user.dto;

import com.github.kzhunmax.jobsearch.shared.enums.Role;
import com.github.kzhunmax.jobsearch.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(
        description = "User data response object containing basic user information",
        example = """
                {
                    "email": "user@xample.com"
                    "roles": ["ROLE_CANDIDATE"]
                }
                """
)
public record UserResponseDTO(

        @Schema(description = "User's email address", example = "user@example", format = "email")
        String email,

        @Schema(description = "Set of user roles defining access permissions", example = "[\"ROLE_CANDIDATE\"]")
        Set<Role> roles
) {
}

