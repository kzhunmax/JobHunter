package com.github.kzhunmax.jobsearch.controller;

import com.github.kzhunmax.jobsearch.dto.request.UserLoginDTO;
import com.github.kzhunmax.jobsearch.dto.request.UserRegistrationDTO;
import com.github.kzhunmax.jobsearch.dto.response.JwtResponse;
import com.github.kzhunmax.jobsearch.dto.response.UserResponseDTO;
import com.github.kzhunmax.jobsearch.exception.ApiException;
import com.github.kzhunmax.jobsearch.payload.ApiResponse;
import com.github.kzhunmax.jobsearch.service.AuthService;
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

    @PostMapping("/register")
    @Operation(
            summary = "Register new user",
            description = "Creates a new user account with the provided details and returns user information"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "User successfully registered",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "data": {
                                                "username": "user",
                                                "email": "user@example.com",
                                                "roles": [
                                                  "ROLE_CANDIDATE"
                                                ]
                                              },
                                              "errors": [],
                                              "timestamp": "2025-09-22T10:15:30Z",
                                              "requestId": "request-123"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data or validation errors",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "data": null,
                                              "errors": [
                                                {
                                                  "code": "INVALID_DATA",
                                                  "message": "Passwords don't match"
                                                }
                                              ],
                                              "timestamp": "2025-09-22T10:15:30Z",
                                              "requestId": "request-123"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "User with provided username or email already exists",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "data": null,
                                              "errors": [
                                                {
                                                  "code": "USERNAME_TAKEN",
                                                  "message": "Username recruiter is already taken"
                                                }
                                              ],
                                              "timestamp": "2025-09-22T10:15:30Z",
                                              "requestId": "request-123"
                                            }
                                            """
                            )
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
        log.info("Request [{}]: Processing registration request for username={}", requestId, userRegistrationDTO.username());

        UserResponseDTO user = authService.registerUser(userRegistrationDTO);
        log.info("Request [{}]: Successfully registered for username={}", requestId, user.username());
        return ApiResponse.created(user, requestId);
    }

    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticates user credentials and returns JWT access & refresh tokens"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "data": {
                                                "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMSIsImlhdCI6MTc1OTA2OTA3MywiZXhwIjoxNzU5MDY5OTczfQ.3BBCmH6sNnPoRcp5tOUQ30HV5kYm8jxCeGeC3cFueG4",
                                                "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMSIsImlhdCI6MTc1OTA2OTA3MywiZXhwIjoxNzU5NjczODczfQ._Q5GWHUTlcVBZo1AxxewpcMZu_DN658cRrKUTzBb5kc",
                                                "tokenType": "Bearer",
                                                "issuedAt": "2025-09-22T10:15:30Z",
                                                "expiresAt": "2025-09-22T11:15:30Z"
                                              },
                                              "errors": [],
                                              "timestamp": "2025-09-22T10:15:30Z",
                                              "requestId": "request-123"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Missing or invalid credentials format",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "data": null,
                                                "errors": [
                                                  {
                                                    "code": "VALIDATION_FAILED",
                                                    "message": "Validation failed"
                                                  }
                                                ],
                                              "timestamp": "2025-09-22T10:15:30Z",
                                              "requestId": "request-123"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid username/email or password",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "data": null,
                                              "errors": [
                                                {
                                                  "code": "AUTH_FAILED",
                                                  "message": "Invalid username or password"
                                                }
                                              ],
                                              "timestamp": "2025-09-22T10:15:30Z",
                                              "requestId": "request-123"
                                            }
                                            """
                            )
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
        log.info("Request [{}]: Login attempt for username={}", requestId, loginDto.usernameOrEmail());

        JwtResponse jwtResponse = authService.authenticate(loginDto.usernameOrEmail(), response);

        log.info("Request [{}]: Successful login for username={}", requestId, loginDto.usernameOrEmail());

        return ApiResponse.success(jwtResponse, requestId);
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh JWT token",
            description = "Generates new access and refresh tokens using a valid refresh token"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Token successfully refreshed",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "data": {
                                                  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ...",
                                                  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ...",
                                                  "tokenType": "Bearer",
                                                  "issuedAt": "2025-09-22T10:15:30Z",
                                                  "expiresAt": "2025-09-22T11:15:30Z"
                                              },
                                              "errors": [],
                                              "timestamp": "2025-09-22T10:15:30Z",
                                              "requestId": "request-123"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Missing refresh token in request",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "data": null,
                                              "errors": [
                                                {
                                                  "code": "MISSING_TOKEN",
                                                  "message": "Refresh token is required"
                                                }
                                              ],
                                              "timestamp": "2025-09-22T10:15:30Z",
                                              "requestId": "request-123"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired refresh token",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "data": null,
                                              "errors": [
                                                {
                                                  "code": "INVALID_REFRESH",
                                                  "message": "Refresh token is invalid or expired"
                                                }
                                              ],
                                              "timestamp": "2025-09-22T10:15:30Z",
                                              "requestId": "request-123"
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<JwtResponse>> refresh(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token request",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
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

    @GetMapping("/main")
    public String mainPage() {
        return "You successfully login to main page";
    }
}
