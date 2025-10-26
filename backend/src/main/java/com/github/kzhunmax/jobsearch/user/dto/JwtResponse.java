package com.github.kzhunmax.jobsearch.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(
        description = "JWT authentication tokens response",
        example = """
                {
                    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ...",
                    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ...",
                    "tokenType": "Bearer",
                    "issuedAt": "2025-09-22T10:15:30Z",
                    "expiresAt": "2025-09-22T11:15:30Z"
                }
                """
)
public record JwtResponse(

        @Schema(
                description = "JWT access token for API authentication",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ..."
        )
        String accessToken,

        @Schema(
                description = "Refresh token to obtain new access token",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ..."
        )
        String refreshToken,

        @Schema(
                description = "Type of token (always 'Bearer' for this API",
                example = "Bearer"
        )
        String tokenType,

        @Schema(
                description = "Timestamp when the access token was issued (ISO-8601 format)",
                example = "2025-09-22T10:15:30Z"
        )
        Instant issuedAt,

        @Schema(
                description = "Expiration timestamp for the access token (ISO-8601 format)",
                example = "2025-10-22T10:15:30Z"
        )
        Instant expiresAt
) {
}
