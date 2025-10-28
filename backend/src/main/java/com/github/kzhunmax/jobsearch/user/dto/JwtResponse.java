package com.github.kzhunmax.jobsearch.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "JWT authentication tokens response")
public record JwtResponse(

        @Schema(description = "JWT access token for API authentication")
        String accessToken,

        @Schema(description = "Refresh token to obtain new access token")
        String refreshToken,

        @Schema(description = "Type of token (always 'Bearer' for this API", example = "Bearer")
        String tokenType,

        @Schema(description = "Timestamp when the access token was issued (ISO-8601 format)")
        Instant issuedAt,

        @Schema(description = "Expiration timestamp for the access token (ISO-8601 format)")
        Instant expiresAt
) {
}
