package com.github.kzhunmax.jobsearch.user.validator;

import com.github.kzhunmax.jobsearch.exception.EmailExistsException;
import com.github.kzhunmax.jobsearch.user.dto.UserRegistrationDTO;
import com.github.kzhunmax.jobsearch.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.regex.Pattern;

import static com.github.kzhunmax.jobsearch.shared.enums.Role.ROLE_CANDIDATE;
import static com.github.kzhunmax.jobsearch.util.TestDataFactory.TEST_EMAIL;
import static com.github.kzhunmax.jobsearch.util.TestDataFactory.createUserRegistrationDTO;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserRegistrationValidator Unit Tests")
public class UserRegistrationValidatorTest {

    @Mock
    private UserRepository userRepository;

    private UserRegistrationValidator validator;

    private UserRegistrationDTO validDto;

    @BeforeEach
    void setUp() {
        Pattern passwordPattern = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");
        Pattern emailPattern = Pattern.compile("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
        validator = new UserRegistrationValidator(userRepository, passwordPattern, emailPattern);
        validDto = createUserRegistrationDTO();
    }

    @Test
    @DisplayName("should pass validation when all data is valid")
    void validateRegistration_shouldPass_whenValid() {
        // Arrange
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);

        // Act & Assert
        assertThatNoException().isThrownBy(() -> validator.validateRegistration(validDto));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when passwords do not match")
    void validateRegistration_shouldThrow_whenPasswordsMismatch() {
        UserRegistrationDTO dto = new UserRegistrationDTO(TEST_EMAIL, "Password123", "Mismatch", Set.of(ROLE_CANDIDATE));

        assertThatThrownBy(() -> validator.validateRegistration(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Passwords don't match");
    }

    @ParameterizedTest
    @ValueSource(strings = {"weak", "password", "Password", "PASSWORD123"})
    @DisplayName("should throw when password format is invalid")
    void validateRegistration_shouldThrow_whenInvalidPasswordFormat(String invalidPassword) {
        UserRegistrationDTO dto = new UserRegistrationDTO(TEST_EMAIL, invalidPassword, invalidPassword, Set.of(ROLE_CANDIDATE));

        assertThatThrownBy(() -> validator.validateRegistration(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password must be at least 8 characters long and include uppercase, lowercase, and a number");
    }

    @Test
    @DisplayName("should throw EmailExistsException when email is taken")
    void validateRegistration_shouldThrow_whenEmailTaken() {
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> validator.validateRegistration(validDto))
                .isInstanceOf(EmailExistsException.class)
                .hasMessage("Email " + TEST_EMAIL + " is already taken");
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-email", "user@domain", "user@.com", "user@domain."})
    @DisplayName("should throw when email format is invalid")
    void validateRegistration_shouldThrow_whenInvalidEmailFormat(String invalidEmail) {
        UserRegistrationDTO dto = new UserRegistrationDTO(invalidEmail, "Password123", "Password123", Set.of(ROLE_CANDIDATE));
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        assertThatThrownBy(() -> validator.validateRegistration(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email format");
    }
}