package com.github.kzhunmax.jobsearch.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.github.kzhunmax.jobsearch.util.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    @Spy
    @InjectMocks
    private JwtService jwtService;

    private static final String SECRET_KEY = Base64.getEncoder()
            .encodeToString("test-secret-key-base64-encoded-for-testing-purposes".getBytes());
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        // Arrange
        userDetails = createUserDetails(TEST_EMAIL);
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", JWT_EXPIRATION);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", REFRESH_EXPIRATION);
    }

    @Test
    @DisplayName("should generate a valid token from user details")
    void generateToken_withUserDetails_createValidToken() {
        // Act
        String token = jwtService.generateToken(userDetails);

        // Assert
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("should extract email from a valid token")
    void extractUsername_fromValidToken_returnsUsername() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        String extracted = jwtService.extractEmail(token);

        // Assert
        assertThat(extracted).isEqualTo(TEST_EMAIL);
    }

    @Test
    @DisplayName("should extract subject claim")
    void extractClaim_withResolver_returnsClaimValue() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        String result = jwtService.extractClaim(token, Claims::getSubject);

        // Assert
        assertThat(result).isEqualTo(TEST_EMAIL);
    }

    @Test
    @DisplayName("should build token with extra claims")
    void buildToken_withExtraClaims_buildsToken() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "admin");

        String token = jwtService.buildToken(extraClaims, userDetails);
        String role = jwtService.extractClaim(token, claims -> (String) claims.get("role"));
        assertThat(role).isEqualTo("admin");
    }

    @Test
    @DisplayName("should generate a refresh token")
    void generateRefreshToken_createsRefreshToken() {
        // Act
        String token = jwtService.generateRefreshToken(userDetails);

        // Assert
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("should return true for a valid token")
    void isTokenValid_validTokenAndUser_ReturnsTrue() {
        String token = jwtService.generateToken(userDetails);
        boolean valid = jwtService.isTokenValid(token, userDetails);
        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("should return false for an expired token")
    void isTokenValid_shouldReturnFalse_forExpiredToken() {
        // Arrange
        String expiredToken = "any-fake-token-string";
        doReturn(new Date(System.currentTimeMillis() - 10000)).when(jwtService).extractClaim(eq(expiredToken), any());
        doReturn(TEST_EMAIL).when(jwtService).extractEmail(expiredToken);
        // Act
        boolean valid = jwtService.isTokenValid(expiredToken, userDetails);
        // Assert
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("should return false for token with wrong user")
    void isTokenValid_wrongUsername_returnsFalse() {
        String token = jwtService.generateToken(userDetails);
        UserDetails wrongUser = createUserDetails(NON_EXISTENT_EMAIL);
        boolean valid = jwtService.isTokenValid(token, wrongUser);
        assertThat(valid).isFalse();
    }
}
