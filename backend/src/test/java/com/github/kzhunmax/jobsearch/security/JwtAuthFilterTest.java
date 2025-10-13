package com.github.kzhunmax.jobsearch.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;

import static com.github.kzhunmax.jobsearch.util.TestDataFactory.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    private MockedStatic<SecurityContextHolder> securityContextHolderMockStatic;

    @BeforeEach
    void setUp() {
        securityContextHolderMockStatic = mockStatic(SecurityContextHolder.class);
        securityContextHolderMockStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
    }

    @AfterEach
    void tearDown() {
        securityContextHolderMockStatic.close();
    }

    @Test
    void doFilterInternal_withNoJwtHeader_skipsAuthenticationAndCallsChain() throws IOException, ServletException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, mockChain());

        verify(jwtService, never()).extractUsername(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    void doFilterInternal_withInvalidHeaderFormat_skipsAuthentication() throws IOException, ServletException {
        when(request.getHeader("Authorization")).thenReturn("Invalid");

        jwtAuthFilter.doFilterInternal(request, response, mockChain());

        verify(jwtService, never()).extractUsername(anyString());
    }

    @Test
    void doFilterInternal_withValidJwt_extractsUsernameAndAuthenticates() throws IOException, ServletException {
        UserDetails userDetails = createUserDetails(TEST_USERNAME);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_JWT);
        when(jwtService.extractUsername(VALID_JWT)).thenReturn(TEST_USERNAME);
        when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(null);
        when(userDetailsService.loadUserByUsername(TEST_USERNAME)).thenReturn(userDetails);
        when(jwtService.isTokenValid(VALID_JWT, userDetails)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, mockChain());

        verify(userDetailsService).loadUserByUsername(TEST_USERNAME);
        verify(jwtService).isTokenValid(VALID_JWT, userDetails);
        verify(SecurityContextHolder.getContext()).setAuthentication(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void doFilterInternal_jwtProcessingException_logsWarningAndContinues() throws IOException, ServletException {
        FilterChain chain = mockChain();
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_JWT);
        when(jwtService.extractUsername(VALID_JWT)).thenThrow(new RuntimeException("Test exception"));

        jwtAuthFilter.doFilterInternal(request, response, chain);

        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(chain).doFilter(any(), any());
    }

    private FilterChain mockChain() throws ServletException, IOException {
        FilterChain chain = mock(FilterChain.class);
        doNothing().when(chain).doFilter(any(), any());
        return chain;
    }
}
