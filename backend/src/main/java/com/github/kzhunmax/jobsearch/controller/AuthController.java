package com.github.kzhunmax.jobsearch.controller;

import com.github.kzhunmax.jobsearch.dto.request.UserLoginDTO;
import com.github.kzhunmax.jobsearch.dto.request.UserRegistrationDTO;
import com.github.kzhunmax.jobsearch.dto.response.JwtResponse;
import com.github.kzhunmax.jobsearch.dto.response.UserResponseDTO;
import com.github.kzhunmax.jobsearch.payload.ApiResponse;
import com.github.kzhunmax.jobsearch.security.JwtService;
import com.github.kzhunmax.jobsearch.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private static final String REQUEST_ID_MDC_KEY = "requestId";

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDTO>> registerUser(
            @Valid @RequestBody UserRegistrationDTO userRegistrationDTO) {

        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Processing registration request | requestId={}, username={}", requestId, userRegistrationDTO.username());

        UserResponseDTO user = authService.registerUser(userRegistrationDTO);
        log.info("Successfully registered user | requestId={} username={}", requestId, user.username());
        return ApiResponse.created(user, requestId);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody UserLoginDTO loginDto) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Login attempt | requestId={}, username={}", requestId, loginDto.usernameOrEmail());

        UserDetails userDetails = userDetailsService.loadUserByUsername(loginDto.usernameOrEmail());
        String jwtToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        log.info("Successful login | requestId={}, username={}", requestId, userDetails.getUsername());
        return ApiResponse.success(new JwtResponse(jwtToken, refreshToken), requestId);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtResponse>> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        String username = jwtService.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (jwtService.isTokenValid(refreshToken, userDetails)) {
            String newAccessToken = jwtService.generateToken(userDetails);
            return ApiResponse.success(new JwtResponse(newAccessToken, refreshToken), MDC.get(REQUEST_ID_MDC_KEY));
        } else {
            return ApiResponse.error(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH", "Refresh token is invalid or expired", MDC.get(REQUEST_ID_MDC_KEY));
        }
    }
}
