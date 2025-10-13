package com.github.kzhunmax.jobsearch.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
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
        userDetails = createUserDetails(TEST_USERNAME);
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", JWT_EXPIRATION);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", REFRESH_EXPIRATION);
    }

    @Test
    void generateToken_withUserDetails_createValidToken() {
        String token = jwtService.generateToken(userDetails);
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    void extractUsername_fromValidToken_returnsUsername() {
        String token = jwtService.generateToken(userDetails);
        String extracted = jwtService.extractUsername(token);
        assertThat(extracted).isEqualTo(TEST_USERNAME);
    }

    @Test
    void extractClaim_withResolver_returnsClaimValue() {
        String token = jwtService.generateToken(userDetails);
        String result = jwtService.extractClaim(token, Claims::getSubject);
        assertThat(result).isEqualTo(TEST_USERNAME);
    }

    @Test
    void buildToken_withExtraClaims_buildsToken() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "admin");

        String token = jwtService.buildToken(extraClaims, userDetails);

        String role = jwtService.extractClaim(token, claims -> (String) claims.get("role"));
        assertThat(role).isEqualTo("admin");
    }

    @Test
    void generateRefreshToken_createsRefreshToken() {
        String token = jwtService.generateRefreshToken(userDetails);

        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    void isTokenValid_validTokenAndUser_ReturnsTrue() {
        String token = jwtService.generateToken(userDetails);
        boolean valid = jwtService.isTokenValid(token, userDetails);
        assertThat(valid).isTrue();
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        String expiredToken = "any-fake-token-string";
        doReturn(new Date(System.currentTimeMillis() - 10000)).when(jwtService).extractClaim(eq(expiredToken), any());
        doReturn(TEST_USERNAME).when(jwtService).extractUsername(expiredToken);

        boolean valid = jwtService.isTokenValid(expiredToken, userDetails);
        assertThat(valid).isFalse();
    }

    @Test
    void isTokenValid_wrongUsername_returnsFalse() {
        String token = jwtService.generateToken(userDetails);
        UserDetails wrongUser = createUserDetails("wrongUser");
        boolean valid = jwtService.isTokenValid(token, wrongUser);
        assertThat(valid).isFalse();
    }
}
