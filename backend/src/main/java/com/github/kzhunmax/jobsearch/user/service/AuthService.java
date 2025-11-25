package com.github.kzhunmax.jobsearch.user.service;

import com.github.kzhunmax.jobsearch.event.producer.UserEventProducer;
import com.github.kzhunmax.jobsearch.exception.InvalidOrExpiredTokenException;
import com.github.kzhunmax.jobsearch.security.JwtService;
import com.github.kzhunmax.jobsearch.security.UserDetailsServiceImpl;
import com.github.kzhunmax.jobsearch.shared.CookieService;
import com.github.kzhunmax.jobsearch.shared.RepositoryHelper;
import com.github.kzhunmax.jobsearch.shared.enums.AuthProvider;
import com.github.kzhunmax.jobsearch.shared.enums.Role;
import com.github.kzhunmax.jobsearch.shared.event.EventType;
import com.github.kzhunmax.jobsearch.shared.event.PasswordResetEvent;
import com.github.kzhunmax.jobsearch.shared.event.UserEvent;
import com.github.kzhunmax.jobsearch.user.dto.JwtResponse;
import com.github.kzhunmax.jobsearch.user.dto.ResetPasswordRequestDTO;
import com.github.kzhunmax.jobsearch.user.dto.UserRegistrationDTO;
import com.github.kzhunmax.jobsearch.user.dto.UserResponseDTO;
import com.github.kzhunmax.jobsearch.user.mapper.UserMapper;
import com.github.kzhunmax.jobsearch.user.model.User;
import com.github.kzhunmax.jobsearch.user.repository.UserRepository;
import com.github.kzhunmax.jobsearch.user.validator.UserRegistrationValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
    private final AuthenticationManager authenticationManager;
    private final RepositoryHelper repositoryHelper;


    @Transactional
    public UserResponseDTO registerUser(UserRegistrationDTO dto) {
        log.info("Registering user - email={}", dto.email());
        userRegistrationValidator.validateRegistration(dto);
        Set<Role> roles = resolveRoles(dto.roles());
        User user = userMapper.toEntity(dto, roles);
        user.setEmailVerifyToken(UUID.randomUUID().toString());
        user.generateApiKey();
        User savedUser = userRepository.save(user);
        UserEvent event = new UserEvent(dto.email(), EventType.REGISTERED, user.getEmailVerifyToken());
        userEventProducer.sendUserEvent(event);
        log.info("User registered successfully - email={}", dto.email());
        return userMapper.toDto(savedUser);
    }

    public JwtResponse authenticate(String email, String password, HttpServletResponse response) {
        log.info("Authenticating user - email={}", email);
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        if (!userDetails.isEnabled()) {
            log.warn("Authentication rejected - account disabled (email unverified): {}", email);
            throw new DisabledException("Please verify your email address before logging in. Check your inbox for the verification link, or use the 'Resend Verification' option.");
        }
        log.info("User authenticated successfully - email={}", email);
        return issueTokens(userDetails, response);
    }

    public JwtResponse refreshTokens(String refreshToken, HttpServletResponse response) {
        log.info("Refreshing tokens");
        String email = jwtService.extractEmail(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new InvalidOrExpiredTokenException();
        }
        log.info("Tokens refreshed successfully - email={}", email);
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

    @Transactional
    public void forgotPassword(String email) {
        log.info("Password reset requested for email={}", email);

        userRepository.findByEmail(email)
                .filter(user -> user.getProvider() == AuthProvider.LOCAL)
                .ifPresent(user -> generateAndSendToken(user, EventType.PASSWORD_RESET));

        log.info("Password reset request processed for email={}", email);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequestDTO dto) {
        log.info("Attempting to reset password with token={}", dto.token());

        if (!dto.isPasswordConfirmed()) {
            throw new IllegalArgumentException("Passwords don't match");
        }
        User user = userRepository.findByResetPasswordToken(dto.token())
                .orElseThrow(() -> new InvalidOrExpiredTokenException("Invalid or expired reset token", HttpStatus.BAD_REQUEST, "INVALID_TOKEN"));

        if (user.getResetPasswordTokenExpiry() == null || user.getResetPasswordTokenExpiry().isBefore(Instant.now())) {
            throw new InvalidOrExpiredTokenException("Invalid or expired reset token", HttpStatus.BAD_REQUEST, "INVALID_TOKEN");
        }
        user.setPassword(userMapper.encodePassword(dto.newPassword()));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        userRepository.save(user);

        log.info("Password reset successfully for user={}", user.getEmail());
    }

    @Transactional
    public void verifyEmail(String token) {
        log.info("Attempting to verify email with token={}", token);

        User user = userRepository.findByEmailVerifyToken(token)
                .orElseThrow(() -> new InvalidOrExpiredTokenException("Invalid or expired verification token", HttpStatus.BAD_REQUEST, "INVALID_TOKEN"));

        user.setEmailVerified(true);
        user.setEmailVerifyToken(null);
        userRepository.save(user);

        log.info("Email verified successfully for user={}", user.getEmail());
    }

    @Transactional
    public void resendVerification(String email) {
        log.info("Resend verification requested for email={}", email);

        userRepository.findByEmail(email)
                .filter(user -> user.getProvider() == AuthProvider.LOCAL && !user.isEmailVerified())
                .ifPresent(user -> generateAndSendToken(user, EventType.REGISTERED));

        log.info("Resend verification request processed for email={}", email);
    }

    private void generateAndSendToken(User user, EventType type) {
        String token = UUID.randomUUID().toString();
        if (type == EventType.PASSWORD_RESET) {
            user.setResetPasswordToken(token);
            user.setResetPasswordTokenExpiry(Instant.now().plus(1, ChronoUnit.HOURS));
            userEventProducer.sendPasswordResetEvent(new PasswordResetEvent(user.getEmail(), token));
        } else {
            user.setEmailVerifyToken(token);
            userEventProducer.sendUserEvent(new UserEvent(user.getEmail(), type, token));
        }
        userRepository.save(user);
        log.info("{} token generated for email={}", type, user.getEmail());
    }

    public String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forward-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }

        return xfHeader.split(",")[0].trim();
    }

    @Transactional
    public JwtResponse switchUserRole(Long id, HttpServletResponse response) {
        User user = repositoryHelper.findUserById(id);

        Set<Role> roles = user.getRoles();
        if (roles.contains(Role.ROLE_RECRUITER)) {
            roles.remove(Role.ROLE_RECRUITER);
            roles.add(Role.ROLE_CANDIDATE);
        } else {
            roles.remove(Role.ROLE_CANDIDATE);
            roles.add(Role.ROLE_RECRUITER);
        }

        user.setRoles(roles);
        User savedUser = userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        return issueTokens(userDetails, response);
    }
}
