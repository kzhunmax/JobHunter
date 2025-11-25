package com.github.kzhunmax.jobsearch.user.controller;

import com.github.kzhunmax.jobsearch.payload.ApiResponse;
import com.github.kzhunmax.jobsearch.security.PricingPlan;
import com.github.kzhunmax.jobsearch.security.RateLimitingService;
import com.github.kzhunmax.jobsearch.security.UserDetailsImpl;
import com.github.kzhunmax.jobsearch.user.dto.*;
import com.github.kzhunmax.jobsearch.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Endpoints for user registration, login, and token management")
public class AuthController {

    private final AuthService authService;
    private final RateLimitingService rateLimitingService;

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Register new user",
            description = "Creates a new user account with the provided details and returns user information"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "User successfully registered",
                    useReturnTypeSchema = true
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data or validation errors",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "User with provided email already exists",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    public ResponseEntity<ApiResponse<UserResponseDTO>> registerUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User registration data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserRegistrationDTO.class))
            )
            @Valid @RequestBody UserRegistrationDTO userRegistrationDTO) {

        log.info("Processing registration request for email={}", userRegistrationDTO.email());
        UserResponseDTO user = authService.registerUser(userRegistrationDTO);
        log.info("Successfully registered for email={}", user.email());
        return ApiResponse.created(user);
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "User login",
            description = "Authenticates user credentials and returns JWT access & refresh tokens"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    useReturnTypeSchema = true
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Missing or invalid credentials format",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid email or password",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    public ResponseEntity<ApiResponse<JwtResponse>> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User login credentials",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserLoginDTO.class))
            )
            @Valid @RequestBody UserLoginDTO loginDto,
            HttpServletResponse response,
            HttpServletRequest request
    ) {
        String ipAddress = authService.getClientIp(request);
        rateLimitingService.consumeToken(ipAddress, PricingPlan.FREE, "IP_ADDRESS");
        log.info("Login attempt for email={}", loginDto.email());
        JwtResponse jwtResponse = authService.authenticate(loginDto.email(), loginDto.password(), response);
        log.info("Successful login for email={}", loginDto.email());
        return ApiResponse.success(jwtResponse);
    }

    @PostMapping(value = "/refresh", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Refresh JWT token",
            description = "Generates new access and refresh tokens using a valid refresh token"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Token successfully refreshed",
                    useReturnTypeSchema = true
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Missing refresh token in request",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired refresh token",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    public ResponseEntity<ApiResponse<JwtResponse>> refresh(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token request",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = "{\"refreshToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"}"
                            )
                    )
            )
            @RequestBody Map<String, String> request,
            HttpServletResponse response
    ) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null) {
            log.warn("Missing refresh token");
            return ApiResponse.error(HttpStatus.BAD_REQUEST, "MISSING_TOKEN", "Refresh token is required");
        }

        JwtResponse jwtResponse = authService.refreshTokens(refreshToken, response);
        log.info("Tokens refreshed successfully");
        return ApiResponse.success(jwtResponse);
    }

    @PostMapping(value = "/forgot-password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Request password reset",
            description = "Sends a password reset link to the user's email if the account exists"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Request processed. If an account exists, an email will be sent.",
                    useReturnTypeSchema = true
            )
    })
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDTO dto
    ) {
        authService.forgotPassword(dto.email());
        return ApiResponse.success("A password reset link has been sent.");
    }

    @PostMapping(value = "/reset-password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Reset password",
            description = "Sets a new password using a valid reset token"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Password has been reset successfully",
                    useReturnTypeSchema = true
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid or expired token, or passwords do not match",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDTO dto
    ) {
        authService.resetPassword(dto);
        return ApiResponse.success("Password has been reset successfully.");
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Verify user email", description = "Verifies a user's email address using the provided token.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Email verified successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    public ResponseEntity<ApiResponse<String>> verifyEmail(@RequestParam("token") String token) {
        authService.verifyEmail(token);
        return ApiResponse.success("Email verified successfully.");
    }

    @PostMapping(value = "/resend-verification", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Resend verification email",
            description = "Sends a new verification email to a user who has not yet verified their account."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Request processed. If an unverified account exists, an email will be sent.",
                    useReturnTypeSchema = true
            )
    })
    public ResponseEntity<ApiResponse<String>> resendVerification(
            @Valid @RequestBody ForgotPasswordRequestDTO dto
    ) {
        authService.resendVerification(dto.email());
        return ApiResponse.success("If an unverified account with this email exists, a new verification link has been sent.");
    }

    @PostMapping("/switch-role")
    @Operation(summary = "Switch between Candidate and Recruiter", description = "Toggles the role and returns NEW tokens")
    public ResponseEntity<ApiResponse<JwtResponse>> switchRole(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            HttpServletResponse response
    ) {
        JwtResponse newTokens = authService.switchUserRole(userDetails.getId(), response);
        return ApiResponse.success(newTokens);
    }
}
