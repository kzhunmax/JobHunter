package com.github.kzhunmax.jobsearch.repository;

import com.github.kzhunmax.jobsearch.model.Job;
import com.github.kzhunmax.jobsearch.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@DisplayName("Tests for the JobRepository")
public class JobRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.6");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("user");
        testUser.setEmail("user@example.com");
        testUser.setPassword("Password123");
        userRepository.save(testUser);

        Job activeJob = new Job();
        activeJob.setTitle("Active Job");
        activeJob.setCompany("BigTech");
        activeJob.setLocation("Remote");
        activeJob.setSalary(2000.0);
        activeJob.setActive(true);
        activeJob.setPostedBy(testUser);
        jobRepository.save(activeJob);


        Job inactiveJob = new Job();
        inactiveJob.setTitle("Inactive Job");
        inactiveJob.setCompany("BigTech");
        inactiveJob.setLocation("Remote");
        inactiveJob.setSalary(2000.0);
        inactiveJob.setActive(false);
        inactiveJob.setPostedBy(testUser);
        jobRepository.save(inactiveJob);
    }

    @Test
    @DisplayName("Should find all active jobs with pagination")
    void findByActiveTrue_whenJobExists_shouldReturnOnlyActiveJobs() {
        Page<Job> jobs = jobRepository.findByActiveTrue(PageRequest.of(0, 10));

        assertThat(jobs).hasSize(1);
        assertThat(jobs.getContent().getFirst().getTitle()).isEqualTo("Active Job");
    }

    @Test
    @DisplayName("Should find all active jobs with pagination")
    void findByPostedBy_whenUserExist_shouldReturnAll() {
        Page<Job> jobs = jobRepository.findByPostedBy(testUser, PageRequest.of(0, 10));

        assertThat(jobs).hasSize(2);
    }
}
