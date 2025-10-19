package com.github.kzhunmax.jobsearch.security;


import com.github.kzhunmax.jobsearch.model.User;
import com.github.kzhunmax.jobsearch.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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
public class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = createUser(TEST_ID, TEST_USERNAME);
    }

    @Test
    void loadUserByUsername_userExists_returnsUserDetails() {
        when(userRepository.findByEmail(TEST_USERNAME)).thenReturn(Optional.of(testUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername(TEST_USERNAME);

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(userDetails.getPassword()).isEqualTo("Password123");
        assertThat(userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("CANDIDATE")));
    }

    @Test
    void loadByUsername_userByEmailExists_returnsUserDetails() {
        when(userRepository.findByUsernameOrEmail(TEST_EMAIL, TEST_EMAIL)).thenReturn(Optional.of(testUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername(TEST_EMAIL);

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(userDetails.getPassword()).isEqualTo("Password123");
        assertThat(userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("CANDIDATE")));
    }

    @Test
    void loadUserByUsername_userNotFound_throwsException() {
        when(userRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(NON_EXISTENT_USERNAME))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found with username or email: " + NON_EXISTENT_USERNAME);
    }
}
