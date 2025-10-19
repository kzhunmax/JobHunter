package com.github.kzhunmax.jobsearch.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.github.kzhunmax.jobsearch.constants.LoggingConstants.REQUEST_ID_MDC_KEY;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = 7;


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws IOException, ServletException {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.debug("Request [{}]: Processing JWT authentication filter", requestId);
        final String jwt = extractJwtFromRequest(request);

        if (jwt != null) {
            processJwtAuthentication(request, jwt);
        }
        log.debug("Request [{}]: JWT authentication filter completed", requestId);
        filterChain.doFilter(request, response);
    }

    private void processJwtAuthentication(HttpServletRequest request, String jwt) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        try {
            String email = jwtService.extractEmail(jwt);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    log.debug("Request [{}]: User authenticated successfully - email={}", requestId, email);
                    authenticateUser(request, userDetails);
                }
            }
        } catch (Exception e) {
            log.warn("Request [{}]: JWT processing failed - {}", requestId, e.getMessage());
        }
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }

        return authHeader.substring(BEARER_PREFIX_LENGTH);
    }

    private void authenticateUser(HttpServletRequest request, UserDetails userDetails) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}
