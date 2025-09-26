package com.github.kzhunmax.jobsearch.dto.request;

import com.github.kzhunmax.jobsearch.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

@Schema(
        description = "DTO for new user registration",
        example = """
                {
                    "username": "user",
                    "email": "user@example.com",
                    "password": "Password123",
                    "confirmPassword": "Password123",
                    "roles": ["ROLE_CANDIDATE"]
                }
                """
)
public record UserRegistrationDTO(

        @Schema(description = "Unique username", example = "user", minLength = 3, maxLength = 50)
        @NotBlank @Size(min = 3, max = 50) String username,

        @Schema(description = "User email", example = "user@example.com", format = "email")
        @NotBlank @Email String email,

        @Schema(description = "Password from user account", example = "Password123", minLength = 8, maxLength = 100, format = "password")
        @NotBlank @Size(min = 8, max = 100) String password,

        @Schema(description = "Confirmation of user password", example = "Password123", format = "password")
        String confirmPassword,

        @Schema(description = "Roles of user in system", example = "[\"ROLE_CANDIDATE\"]", allowableValues = {"ROLE_CANDIDATE", "ROLE_RECRUITER", "ROLE_ADMIN"})
        @NotEmpty Set<Role> roles
) {
    public boolean isPasswordConfirmed() {
        return password != null && password.equals(confirmPassword);
    }
}
