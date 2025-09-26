package com.github.kzhunmax.jobsearch.dto.response;

import com.github.kzhunmax.jobsearch.model.Role;
import com.github.kzhunmax.jobsearch.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(
        description = "User data response object containing basic user information",
        example = """
                {
                    "username": "user",
                    "email": "user@xample.com"
                    "roles": ["ROLE_CANDIDATE"]
                }
                """
)
public record UserResponseDTO(

        @Schema(description = "Unique username identifier", example = "user", minLength = 3, maxLength = 50)
        String username,

        @Schema(description = "User's email address", example = "user@example", format = "email")
        String email,

        @Schema(description = "Set of user roles defining access permissions", example = "[\"ROLE_CANDIDATE\"]")
        Set<Role> roles
) {
    public static UserResponseDTO fromEntity(User user) {
        return new UserResponseDTO(
                user.getUsername(),
                user.getEmail(),
                user.getRoles()
        );
    }
}

