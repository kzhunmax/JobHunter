package com.github.kzhunmax.jobsearch.service;

import com.github.kzhunmax.jobsearch.dto.request.UserRegistrationDTO;
import com.github.kzhunmax.jobsearch.dto.response.JwtResponse;
import com.github.kzhunmax.jobsearch.dto.response.UserResponseDTO;
import com.github.kzhunmax.jobsearch.event.producer.UserEventProducer;
import com.github.kzhunmax.jobsearch.exception.ApiException;
import com.github.kzhunmax.jobsearch.mapper.UserMapper;
import com.github.kzhunmax.jobsearch.model.Role;
import com.github.kzhunmax.jobsearch.model.User;
import com.github.kzhunmax.jobsearch.model.event.EventType;
import com.github.kzhunmax.jobsearch.model.event.UserEvent;
import com.github.kzhunmax.jobsearch.repository.UserRepository;
import com.github.kzhunmax.jobsearch.security.JwtService;
import com.github.kzhunmax.jobsearch.security.UserDetailsServiceImpl;
import com.github.kzhunmax.jobsearch.validator.UserRegistrationValidator;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static com.github.kzhunmax.jobsearch.constants.LoggingConstants.REQUEST_ID_MDC_KEY;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRegistrationValidator userRegistrationValidator;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CookieService cookieService;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtService jwtService;
    private final UserEventProducer userEventProducer;


    @Transactional
    public UserResponseDTO registerUser(UserRegistrationDTO dto) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Registering user - email={}", requestId, dto.email());
        userRegistrationValidator.validateRegistration(dto);
        Set<Role> roles = resolveRoles(dto.roles());
        User user = userMapper.toEntity(dto, roles);
        User savedUser = userRepository.save(user);
        UserEvent event = new UserEvent(dto.email(), EventType.REGISTERED);
        userEventProducer.sendUserEvent(event);
        log.info("Request [{}]: User registered successfully - email={}", requestId, dto.email());
        return userMapper.toDto(savedUser);
    }

    public JwtResponse authenticate(String email, HttpServletResponse response) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Authenticating user - email={}", requestId, email);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        log.info("Request [{}]: User authenticated successfully - email={}", requestId, email);
        return issueTokens(userDetails, response);
    }

    public JwtResponse refreshTokens(String refreshToken, HttpServletResponse response) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Refreshing tokens", requestId);
        String email = jwtService.extractEmail(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new ApiException("Refresh token is invalid or expired", HttpStatus.UNAUTHORIZED, "INVALID_REFRESH");
        }
        log.info("Request [{}]: Tokens refreshed successfully - email={}", requestId, email);
        return issueTokens(userDetails, response);
    }

    private JwtResponse issueTokens(UserDetails userDetails, HttpServletResponse response) {
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        cookieService.addAuthCookiesToResponse(accessToken, refreshToken, jwtService, response);

        Instant issueAt = Instant.now();
        Instant expiresAt = issueAt.plusMillis(jwtService.getJwtExpiration());

        return new JwtResponse(accessToken, refreshToken, "Bearer", issueAt, expiresAt);
    }

    private Set<Role> resolveRoles(Set<Role> requestedRoles) {
        return new HashSet<>(requestedRoles != null ? requestedRoles : Set.of(Role.ROLE_CANDIDATE));
    }
}
