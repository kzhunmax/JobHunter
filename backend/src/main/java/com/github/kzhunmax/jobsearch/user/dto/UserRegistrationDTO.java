package com.github.kzhunmax.jobsearch.user.dto;

import com.github.kzhunmax.jobsearch.shared.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

@Schema(description = "DTO for new user registration")
public record UserRegistrationDTO(

        @Schema(description = "User email", format = "email")
        @NotBlank @Email String email,

        @Schema(description = "Password from user account", minLength = 8, maxLength = 100, format = "password")
        @NotBlank @Size(min = 8, max = 100) String password,

        @Schema(description = "Confirmation of user password", format = "password")
        String confirmPassword,

        @Schema(description = "Roles of user in system", allowableValues = {"ROLE_CANDIDATE", "ROLE_RECRUITER", "ROLE_ADMIN"})
        @NotEmpty Set<Role> roles
) {
    public boolean isPasswordConfirmed() {
        return password != null && password.equals(confirmPassword);
    }
}
