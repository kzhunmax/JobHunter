package com.github.kzhunmax.jobsearch.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kzhunmax.jobsearch.dto.request.UserLoginDTO;
import com.github.kzhunmax.jobsearch.dto.request.UserRegistrationDTO;
import com.github.kzhunmax.jobsearch.dto.response.UserResponseDTO;
import com.github.kzhunmax.jobsearch.security.JwtAuthFilter;
import com.github.kzhunmax.jobsearch.security.JwtService;
import com.github.kzhunmax.jobsearch.security.UserDetailsServiceImpl;
import com.github.kzhunmax.jobsearch.service.AuthService;
import com.github.kzhunmax.jobsearch.service.CookieService;
import com.github.kzhunmax.jobsearch.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static com.github.kzhunmax.jobsearch.util.TestDataFactory.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController Tests")
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private CookieService cookieService;


    private UserRegistrationDTO registrationDTO;
    private UserResponseDTO userResponseDTO;
    private UserLoginDTO loginDTO;
    private UserDetails userDetails;


    @BeforeEach
    void setUp() {
        registrationDTO = createUserRegistrationDTO();
        userResponseDTO = createUserResponseDTO(TEST_ID);
        loginDTO = createUserLoginDTO();
        userDetails = createUserDetails(TEST_USERNAME);
    }

    @Test
    @DisplayName("Should register user and return created response")
    void registerUser_withValidData_shouldRegisterUserAndReturnCreated() throws Exception {
        when(authService.registerUser(any(UserRegistrationDTO.class))).thenReturn(userResponseDTO);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.username").value(TEST_USERNAME))
                .andExpect(jsonPath("$.data.email").value(TEST_USERNAME + "@example.com"));
    }

    @Test
    @DisplayName("Should login user successfully")
    void login_withValidData_shouldLoginSuccessfully() throws Exception {
        when(userDetailsService.loadUserByUsername(eq(loginDTO.usernameOrEmail())))
                .thenReturn(userDetails);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");
        when(jwtService.generateRefreshToken(any(UserDetails.class))).thenReturn("refresh-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
    }

    @Test
    @DisplayName("Should refresh token successfully when valid token provided")
    void refresh_withValidToken_shouldSuccessfullyRefresh() throws Exception {
        String refreshToken = "valid-refresh";

        when(jwtService.extractUsername(refreshToken)).thenReturn(TEST_USERNAME);
        when(userDetailsService.loadUserByUsername(TEST_USERNAME)).thenReturn(userDetails);
        when(jwtService.isTokenValid(refreshToken, userDetails)).thenReturn(true);
        when(jwtService.generateToken(userDetails)).thenReturn("new-access");
        when(jwtService.generateRefreshToken(userDetails)).thenReturn("new-refresh");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new-access"))
                .andExpect(jsonPath("$.data.refreshToken").value("new-refresh"));
    }

    @Test
    @DisplayName("Should return 401 when refresh token is invalid")
    void refresh_whenInvalidToken_shouldReturnUnauthorized() throws Exception {
        String refreshToken = "invalid-refresh";
        when(jwtService.extractUsername(refreshToken)).thenReturn(TestDataFactory.TEST_USERNAME);
        when(userDetailsService.loadUserByUsername(TestDataFactory.TEST_USERNAME)).thenReturn(userDetails);
        when(jwtService.isTokenValid(refreshToken, userDetails)).thenReturn(false);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errors[0].code").value("INVALID_REFRESH"))
                .andExpect(jsonPath("$.errors[0].message").value("Refresh token is invalid or expired"));
    }

    @Test
    @DisplayName("Should return 400 when refresh token is missing")
    void refresh_whenMissingToken_shouldReturnBadRequest() throws Exception {

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].code").value("MISSING_TOKEN"))
                .andExpect(jsonPath("$.errors[0].message").value("Refresh token is required"));
    }
}
