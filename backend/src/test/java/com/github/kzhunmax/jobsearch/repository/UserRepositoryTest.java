package com.github.kzhunmax.jobsearch.repository;

import com.github.kzhunmax.jobsearch.model.User;
import com.github.kzhunmax.jobsearch.util.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static com.github.kzhunmax.jobsearch.util.TestDataFactory.TEST_USERNAME;
import static com.github.kzhunmax.jobsearch.util.TestDataFactory.createUser;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("UserRepository Tests")
public class UserRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = createUser(TEST_USERNAME);
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("Should return user by username when using findByUsernameOrEmail")
    void findByUsernameOrEmail_whenUsernameMatches_shouldReturnUser() {
        Optional<User> user = userRepository.findByUsernameOrEmail(TEST_USERNAME, "wrong@example.com");

        assertThat(user).isPresent();
        assertThat(user.get().getUsername()).isEqualTo(TEST_USERNAME);
    }

    @Test
    @DisplayName("Should return user by email when using findByUsernameOrEmail")
    void findByUsernameOrEmail_whenEmailMatches_shouldReturnUser() {
        Optional<User> user = userRepository.findByUsernameOrEmail("wrong", testUser.getEmail());

        assertThat(user).isPresent();
        assertThat(user.get().getEmail()).isEqualTo(testUser.getEmail());
    }

    @Test
    @DisplayName("Should return empty optional when neither username nor email matches in findByUsernameOrEmail")
    void findByUsernameOrEmail_whenNeitherMatches_shouldReturnEmpty() {
        Optional<User> user = userRepository.findByUsernameOrEmail("wrong", "wrong@example.com");

        assertThat(user).isEmpty();
    }

    @Test
    @DisplayName("Should return user by username when using findByUsername")
    void findByUsername_whenUserExists_shouldReturnUser() {
        Optional<User> user = userRepository.findByUsername(TEST_USERNAME);

        assertThat(user).isPresent();
        assertThat(user.get().getUsername()).isEqualTo(TEST_USERNAME);
    }

    @Test
    @DisplayName("Should return empty optional when username does not exist in findByUsername")
    void findByUsername_whenUserDoesNotExist_shouldReturnEmpty() {
        Optional<User> user = userRepository.findByUsername("nonexistent");

        assertThat(user).isEmpty();
    }

    @Test
    @DisplayName("Should return true when username exists")
    void existsByUsername_whenUsernameExists_shouldReturnTrue() {
        boolean exists = userRepository.existsByUsername(TEST_USERNAME);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when username does not exist")
    void existsByUsername_whenUsernameDoesNotExist_shouldReturnFalse() {
        boolean exists = userRepository.existsByUsername("nonexistent");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should return true when email exists")
    void existsByEmail_whenEmailExists_shouldReturnTrue() {
        boolean exists = userRepository.existsByEmail(testUser.getEmail());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when email does not exist")
    void existsByEmail_whenEmailDoesNotExist_shouldReturnFalse() {
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        assertThat(exists).isFalse();
    }
}
