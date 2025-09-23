package com.github.kzhunmax.jobsearch.controller;

import com.github.kzhunmax.jobsearch.dto.request.UserLoginDTO;
import com.github.kzhunmax.jobsearch.dto.request.UserRegistrationDTO;
import com.github.kzhunmax.jobsearch.dto.response.JwtResponse;
import com.github.kzhunmax.jobsearch.dto.response.UserResponseDTO;
import com.github.kzhunmax.jobsearch.payload.ApiResponse;
import com.github.kzhunmax.jobsearch.security.JwtService;
import com.github.kzhunmax.jobsearch.security.UserDetailsServiceImpl;
import com.github.kzhunmax.jobsearch.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Endpoints for user registration, login, and token management")
public class AuthController {

    private static final String REQUEST_ID_MDC_KEY = "requestId";

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @PostMapping("/register")
    @Operation(
            summary = "Register new user",
            description = "Creates a new user account with the provided details and returns user information"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "User successfully registered",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data or validation errors"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "User with provided username or email already exists"
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
        log.info("Processing registration request | requestId={}, username={}", requestId, userRegistrationDTO.username());

        UserResponseDTO user = authService.registerUser(userRegistrationDTO);
        log.info("Successfully registered user | requestId={} username={}", requestId, user.username());
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
                    content = @Content(schema = @Schema(implementation = JwtResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Missing or invalid credentials format"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid username/email or password"
            )
    })
    public ResponseEntity<ApiResponse<JwtResponse>> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User login credentials",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserLoginDTO.class))
            )
            @Valid @RequestBody UserLoginDTO loginDto
    ) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Login attempt | requestId={}, username={}", requestId, loginDto.usernameOrEmail());

        UserDetails userDetails = userDetailsService.loadUserByUsername(loginDto.usernameOrEmail());
        String jwtToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        log.info("Successful login | requestId={}, username={}", requestId, userDetails.getUsername());
        return ApiResponse.success(new JwtResponse(jwtToken, refreshToken), requestId);
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
                    content = @Content(schema = @Schema(implementation = JwtResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Missing refresh token in request"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired refresh token"
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
            @RequestBody Map<String, String> request
    ) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null) {
            return ApiResponse.error(HttpStatus.BAD_REQUEST, "MISSING_TOKEN", "Refresh token is required", MDC.get(REQUEST_ID_MDC_KEY));
        }
        try {
            String username = jwtService.extractUsername(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(refreshToken, userDetails)) {
                String newAccessToken = jwtService.generateToken(userDetails);
                String newRefreshToken = jwtService.generateRefreshToken(userDetails);
                return ApiResponse.success(new JwtResponse(newAccessToken, newRefreshToken), MDC.get(REQUEST_ID_MDC_KEY));
            } else {
                return ApiResponse.error(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH", "Refresh token is invalid or expired", MDC.get(REQUEST_ID_MDC_KEY));
            }
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH", "Refresh token is invalid or expired", MDC.get(REQUEST_ID_MDC_KEY));
        }
    }
}
