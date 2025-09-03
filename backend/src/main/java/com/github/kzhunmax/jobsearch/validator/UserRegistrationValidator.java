package com.github.kzhunmax.jobsearch.validator;

import com.github.kzhunmax.jobsearch.dto.request.UserRegistrationDTO;
import com.github.kzhunmax.jobsearch.exception.EmailExistsException;
import com.github.kzhunmax.jobsearch.exception.UsernameExistsException;
import com.github.kzhunmax.jobsearch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class UserRegistrationValidator {

    private final UserRepository userRepository;
    private final Pattern passwordPattern;
    private final Pattern emailPattern;

    public void validateRegistration(UserRegistrationDTO dto) {
        validatePasswordConfirmation(dto);
        validatePasswordFormat(dto);
        validateUsernameUniqueness(dto);
        validateEmailUniqueness(dto);
        validateUsernameNotEmpty(dto);
        validateEmailFormat(dto);
    }

    private void validatePasswordConfirmation(UserRegistrationDTO dto) {
        if (!dto.isPasswordConfirmed()) {
            throw new IllegalArgumentException("Passwords don't match");
        }
    }

    private void validatePasswordFormat(UserRegistrationDTO dto) {
        if (!passwordPattern.matcher(dto.password()).matches()) {
            throw new IllegalArgumentException("Password must be at least 8 characters long and include uppercase, lowercase, and a number");
        }
    }

    private void validateUsernameUniqueness(UserRegistrationDTO dto) {
        if (userRepository.existsByUsername(dto.username().trim())) {
            throw new UsernameExistsException("Username already taken");
        }
    }

    private void validateEmailUniqueness(UserRegistrationDTO dto) {
        if (userRepository.existsByEmail(dto.email().trim())) {
            throw new EmailExistsException("Email already registered");
        }
    }

    private void validateUsernameNotEmpty(UserRegistrationDTO dto) {
        if (dto.username().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
    }

    private void validateEmailFormat(UserRegistrationDTO dto) {
        if (!emailPattern.matcher(dto.email()).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }
}