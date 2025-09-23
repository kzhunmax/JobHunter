package com.github.kzhunmax.jobsearch.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "JWT authentication tokens response",
        example = """
                {
                    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ...",
                    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ...",
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
        String refreshToken
) {
}
