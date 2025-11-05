package com.github.kzhunmax.jobsearch.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kzhunmax.jobsearch.exception.EmailExistsException;
import com.github.kzhunmax.jobsearch.exception.InvalidOrExpiredTokenException;
import com.github.kzhunmax.jobsearch.security.PricingPlan;
import com.github.kzhunmax.jobsearch.security.RateLimitingService;
import com.github.kzhunmax.jobsearch.user.dto.UserLoginDTO;
import com.github.kzhunmax.jobsearch.user.dto.UserRegistrationDTO;
import com.github.kzhunmax.jobsearch.user.dto.JwtResponse;
import com.github.kzhunmax.jobsearch.user.dto.UserResponseDTO;
import com.github.kzhunmax.jobsearch.exception.ApiException;
import com.github.kzhunmax.jobsearch.security.JwtAuthFilter;
import com.github.kzhunmax.jobsearch.user.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

import static com.github.kzhunmax.jobsearch.shared.enums.Role.ROLE_CANDIDATE;
import static com.github.kzhunmax.jobsearch.util.TestDataFactory.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController (@WebMvcTest)")
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private RateLimitingService rateLimitingService;

    private UserRegistrationDTO registrationDTO;
    private UserResponseDTO userResponseDTO;
    private UserLoginDTO loginDTO;
    private JwtResponse jwtResponse;


    @BeforeEach
    void setUp() {
        registrationDTO = createUserRegistrationDTO();
        userResponseDTO = createUserResponseDTO();
        loginDTO = createUserLoginDTO();
        jwtResponse = new JwtResponse(
                ACCESS_TOKEN,
                REFRESH_TOKEN,
                "Bearer",
                Instant.now(),
                Instant.now().plus(1, ChronoUnit.HOURS)
        );

        // Mock rate limiting to always pass
        doNothing().when(rateLimitingService).consumeToken(anyString(), any(PricingPlan.class), anyString());
    }

    @Test
    @DisplayName("registerUser should return 201 Created when data is valid")
    void registerUser_shouldReturnCreated_whenValid() throws Exception {
        when(authService.registerUser(any(UserRegistrationDTO.class))).thenReturn(userResponseDTO);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    @DisplayName("registerUser should return 400 Bad Request when passwords don't match")
    void registerUser_shouldReturnBadRequest_whenPasswordsMismatch() throws Exception {
        UserRegistrationDTO invalidDto = new UserRegistrationDTO(TEST_EMAIL, "Password123", "DifferentPassword", Set.of(ROLE_CANDIDATE));
        when(authService.registerUser(any(UserRegistrationDTO.class)))
                .thenThrow(new IllegalArgumentException("Passwords don't match"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].code").value("INVALID_DATA"))
                .andExpect(jsonPath("$.errors[0].message").value("Passwords don't match"));
    }

    @Test
    @DisplayName("registerUser should return 400 Bad Request when email exists")
    void registerUser_shouldReturnBadRequest_whenEmailExists() throws Exception {
        when(authService.registerUser(any(UserRegistrationDTO.class)))
                .thenThrow(new EmailExistsException(TEST_EMAIL));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].code").value("EMAIL_TAKEN"))
                .andExpect(jsonPath("$.errors[0].message").value("Email " + TEST_EMAIL + " is already taken"));
    }

    @Test
    @DisplayName("login should return 200 OK with tokens when credentials are valid")
    void login_shouldReturnOk_whenCredentialsValid() throws Exception {
        when(authService.authenticate(eq(loginDTO.email()), eq(loginDTO.password()), any())).thenReturn(jwtResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    @DisplayName("login should return 400 Bad Request when email is blank")
    void login_shouldReturnBadRequest_whenEmailBlank() throws Exception {
        UserLoginDTO invalidLoginDTO = new UserLoginDTO("", "Password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLoginDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    @DisplayName("Should return 401 for invalid login credentials")
    void login_withInvalidCredentials_shouldReturnUnauthorized() throws Exception {
        when(authService.authenticate(eq(loginDTO.email()), eq(loginDTO.password()), any()))
                .thenThrow(new ApiException("Invalid username or password", HttpStatus.UNAUTHORIZED, "AUTH_FAILED"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errors[0].code").value("AUTH_FAILED"))
                .andExpect(jsonPath("$.errors[0].message").value("Invalid username or password"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("refresh should return 200 OK with new tokens")
    void refresh_shouldReturnOk_whenValid() throws Exception {

        when(authService.refreshTokens(eq(REFRESH_TOKEN), any())).thenReturn(jwtResponse);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", REFRESH_TOKEN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value(ACCESS_TOKEN));
    }

    @Test
    @DisplayName("refresh should return 401 Unauthorized when token is invalid")
    void refresh_shouldReturnUnauthorized_whenTokenInvalid() throws Exception {
        String invalidToken = "invalid-refresh";

        when(authService.refreshTokens(eq(invalidToken), any())).thenThrow(new InvalidOrExpiredTokenException());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", invalidToken))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errors[0].code").value("INVALID_REFRESH"));
    }
}
