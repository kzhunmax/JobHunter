package com.github.kzhunmax.jobsearch.validator;

import com.github.kzhunmax.jobsearch.user.dto.UserRegistrationDTO;
import com.github.kzhunmax.jobsearch.exception.EmailExistsException;
import com.github.kzhunmax.jobsearch.user.repository.UserRepository;
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
        validateEmailUniqueness(dto);
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

    private void validateEmailUniqueness(UserRegistrationDTO dto) {
        String email = dto.email().trim();
        if (userRepository.existsByEmail(email)) {
            throw new EmailExistsException(email);
        }
    }

    private void validateEmailFormat(UserRegistrationDTO dto) {
        if (!emailPattern.matcher(dto.email()).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }
}