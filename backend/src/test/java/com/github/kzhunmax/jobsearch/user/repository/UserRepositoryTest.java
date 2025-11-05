package com.github.kzhunmax.jobsearch.user.repository;

import com.github.kzhunmax.jobsearch.user.model.User;
import com.github.kzhunmax.jobsearch.util.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static com.github.kzhunmax.jobsearch.util.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("UserRepository (@DataJpaTest)")
public class UserRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        User testUser = createUser(TEST_EMAIL);
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("Should return user by email when using findByEmail")
    void findByEmail_whenEmailMatches_shouldReturnUser() {
        Optional<User> user = userRepository.findByEmail(TEST_EMAIL);

        assertThat(user).isPresent();
        assertThat(user.get().getEmail()).isEqualTo(TEST_EMAIL);
    }

    @Test
    @DisplayName("Should return empty optional when email does not exist in findByEmail")
    void findByEmail_whenUserDoesNotExist_shouldReturnEmpty() {
        Optional<User> user = userRepository.findById(NON_EXISTENT_ID);

        assertThat(user).isEmpty();
    }

    @Test
    @DisplayName("Should return true when email exists")
    void existsByEmail_whenEmailExists_shouldReturnTrue() {
        boolean exists = userRepository.existsByEmail(TEST_EMAIL);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when email does not exist")
    void existsByEmail_whenEmailDoesNotExist_shouldReturnFalse() {
        boolean exists = userRepository.existsByEmail(NON_EXISTENT_EMAIL);

        assertThat(exists).isFalse();
    }
}
