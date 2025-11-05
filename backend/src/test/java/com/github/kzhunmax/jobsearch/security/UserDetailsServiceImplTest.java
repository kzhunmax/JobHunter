package com.github.kzhunmax.jobsearch.security;


import com.github.kzhunmax.jobsearch.user.model.User;
import com.github.kzhunmax.jobsearch.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static com.github.kzhunmax.jobsearch.util.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailsServiceImpl Unit Tests")
public class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = createUser(TEST_ID, TEST_EMAIL);
    }

    @Test
    @DisplayName("should load user by email when user exists")
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        // Arrange
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(TEST_EMAIL);

        // Assert
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(TEST_EMAIL);
        assertThat(userDetails.getPassword()).isEqualTo(testUser.getPassword());
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .contains("ROLE_CANDIDATE");
    }

    @Test
    @DisplayName("should throw UsernameNotFoundException when user does not exist")
    void loadUserByUsername_shouldThrowException_whenUserNotFound() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(NON_EXISTENT_EMAIL))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found with email: " + NON_EXISTENT_EMAIL);
    }
}
