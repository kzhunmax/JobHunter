package com.github.kzhunmax.jobsearch.validator;

import com.github.kzhunmax.jobsearch.dto.request.UserRegistrationDTO;
import com.github.kzhunmax.jobsearch.exception.EmailExistsException;
import com.github.kzhunmax.jobsearch.exception.UsernameExistsException;
import com.github.kzhunmax.jobsearch.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.regex.Pattern;

import static com.github.kzhunmax.jobsearch.util.TestDataFactory.TEST_USERNAME;
import static com.github.kzhunmax.jobsearch.util.TestDataFactory.createUserRegistrationDTO;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserRegistrationValidatorTest {

    @Mock
    private UserRepository userRepository;

    private Pattern passwordPattern;
    private Pattern emailPattern;

    private UserRegistrationValidator validator;

    @BeforeEach
    void setUp() {
        passwordPattern = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");
        emailPattern = Pattern.compile("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$");

        validator = new UserRegistrationValidator(userRepository, passwordPattern, emailPattern);
    }

    @Test
    @DisplayName("Should not throw exception when all validations pass")
    void validateRegistration_whenValidDataProvided_shouldPassValidation() {
        UserRegistrationDTO dto = createUserRegistrationDTO();
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        assertThatNoException().isThrownBy(() -> validator.validateRegistration(dto));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when passwords do not match")
    void validateRegistration_whenPasswordsMismatch_shouldThrowIllegalArgumentException() {
        UserRegistrationDTO dto = new UserRegistrationDTO(TEST_USERNAME, TEST_USERNAME + "@example.com", "Password123", "Mismatch", Set.of());

        assertThatThrownBy(() -> validator.validateRegistration(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Passwords don't match");
    }
    @Test
    @DisplayName("Should throw IllegalArgumentException when password format is invalid")
    void validateRegistration_whenInvalidPasswordFormat_shouldThrowIllegalArgumentException() {
        UserRegistrationDTO dto = new UserRegistrationDTO(TEST_USERNAME, TEST_USERNAME + "@example.com", "weak", "weak", Set.of());

        assertThatThrownBy(() -> validator.validateRegistration(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password must be at least 8 characters long and include uppercase, lowercase, and a number");
    }

    @Test
    @DisplayName("Should throw UsernameExistsException when username is taken")
    void validateRegistration_whenUsernameTaken_shouldThrowUsernameExistsException() {
        UserRegistrationDTO dto = createUserRegistrationDTO();
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(true);

        assertThatThrownBy(() -> validator.validateRegistration(dto))
                .isInstanceOf(UsernameExistsException.class)
                .hasMessage("Username user is already taken");
    }

    @Test
    @DisplayName("Should throw EmailExistsException when email is taken")
    void validateRegistration_whenEmailTaken_shouldThrowEmailExistsException() {
        UserRegistrationDTO dto = createUserRegistrationDTO();
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(TEST_USERNAME + "@example.com")).thenReturn(true);

        assertThatThrownBy(() -> validator.validateRegistration(dto))
                .isInstanceOf(EmailExistsException.class)
                .hasMessage("Email user@example.com is already taken");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when username is empty")
    void validateRegistration_whenUsernameEmpty_shouldThrowIllegalArgumentException() {
        UserRegistrationDTO dto = new UserRegistrationDTO(" ", TEST_USERNAME + "@example.com", "Password123", "Password123", Set.of());

        assertThatThrownBy(() -> validator.validateRegistration(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username cannot be empty");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when email format is invalid")
    void validateRegistration_whenInvalidEmailFormat_shouldThrowIllegalArgumentException() {
        UserRegistrationDTO dto = new UserRegistrationDTO(TEST_USERNAME, "invalid_email", "Password123", "Password123", Set.of());

        assertThatThrownBy(() -> validator.validateRegistration(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email format");
    }

    @Test
    @DisplayName("Should throw exceptions in order when multiple validations fail")
    void validateRegistration_whenMultipleFailures_shouldThrowFirstInOrder() {
        UserRegistrationDTO dto = new UserRegistrationDTO(" ", "invalid_email", "weak", "mismatch", Set.of());

        assertThatThrownBy(() -> validator.validateRegistration(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Passwords don't match");
    }
}