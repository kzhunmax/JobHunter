package com.github.kzhunmax.jobsearch.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO for submitting a new password with a reset token")
public record ResetPasswordRequestDTO(
        @Schema(description = "The password reset token received via email", example = "uuid-token-string")
        @NotBlank(message = "Token is expired")
        String token,

        @Schema(description = "The new password for the account", minLength = 8, maxLength = 100, example = "NewP@ssword123")
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        String newPassword,

        @Schema(description = "Confirmation of the new password", example = "NewP@ssword123")
        @NotBlank(message = "Password confirmation is required")
        String confirmPassword
) {
    public boolean isPasswordConfirmed() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}
