package com.github.kzhunmax.jobsearch.repository;

import com.github.kzhunmax.jobsearch.model.User;
import com.github.kzhunmax.jobsearch.util.AbstractPostgresTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static com.github.kzhunmax.jobsearch.util.TestDataFactory.TEST_USERNAME;
import static com.github.kzhunmax.jobsearch.util.TestDataFactory.createUser;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("UserRepository Tests")
public class UserRepositoryTest extends AbstractPostgresTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = createUser(TEST_USERNAME);
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("Should return user by username")
    void findByUsernameOrEmail_whenUserExist_shouldReturnUserByUsername() {
        Optional<User> user = userRepository.findByUsernameOrEmail(TEST_USERNAME,"wrong@example.com");

        assertThat(user).isPresent();
        assertThat(user.get().getUsername()).isEqualTo(TEST_USERNAME);
    }

    @Test
    @DisplayName("Should return user by email")
    void findByUsernameOrEmail_whenUserExist_shouldReturnUserByEmail() {
        Optional<User> user = userRepository.findByUsernameOrEmail("wrong", TEST_USERNAME + "@example.com");

        assertThat(user).isPresent();
        assertThat(user.get().getEmail()).isEqualTo(TEST_USERNAME + "@example.com");
    }
}
