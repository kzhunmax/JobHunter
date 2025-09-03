package com.github.kzhunmax.jobsearch.dto.request;

import com.github.kzhunmax.jobsearch.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UserRegistrationDTO(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        String confirmPassword,
        @NotEmpty Set<Role> roles
) {
    public boolean isPasswordConfirmed() {
        return password != null && password.equals(confirmPassword);
    }
}
