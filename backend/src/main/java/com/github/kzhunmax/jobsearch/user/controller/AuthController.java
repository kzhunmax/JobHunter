package com.github.kzhunmax.jobsearch.user.controller;

import com.github.kzhunmax.jobsearch.exception.ApiException;
import com.github.kzhunmax.jobsearch.payload.ApiResponse;
import com.github.kzhunmax.jobsearch.user.dto.*;
import com.github.kzhunmax.jobsearch.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.github.kzhunmax.jobsearch.constants.LoggingConstants.REQUEST_ID_MDC_KEY;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Endpoints for user registration, login, and token management")
public class AuthController {

    private final AuthService authService;

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

        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Processing registration request for email={}", requestId, userRegistrationDTO.email());

        UserResponseDTO user = authService.registerUser(userRegistrationDTO);
        log.info("Request [{}]: Successfully registered for email={}", requestId, user.email());
        return ApiResponse.created(user, requestId);
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
            HttpServletResponse response
    ) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Login attempt for email={}", requestId, loginDto.email());

        JwtResponse jwtResponse = authService.authenticate(loginDto.email(), response);

        log.info("Request [{}]: Successful login for email={}", requestId, loginDto.email());

        return ApiResponse.success(jwtResponse, requestId);
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
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);

        if (refreshToken == null) {
            log.warn("Request [{}]: Missing refresh token", requestId);
            return ApiResponse.error(HttpStatus.BAD_REQUEST, "MISSING_TOKEN", "Refresh token is required", requestId);
        }
        try {
            JwtResponse jwtResponse = authService.refreshTokens(refreshToken, response);
            log.info("Request [{}]: Tokens refreshed successfully", requestId);
            return ApiResponse.success(jwtResponse, requestId);
        } catch (ApiException ex) {
            log.warn("Request [{}]: Token refresh failed - {}", requestId, ex.getMessage());
            return ApiResponse.error(ex.getHttpStatus(), ex.getErrorCode(), ex.getMessage(), requestId);
        }
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
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        authService.forgotPassword(dto.email());
        return ApiResponse.success("A password reset link has been sent.", requestId);
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
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        authService.resetPassword(dto);
        return ApiResponse.success("Password has been reset successfully.", requestId);
    }
}
