package com.github.kzhunmax.jobsearch.repository;

import com.github.kzhunmax.jobsearch.model.Job;
import com.github.kzhunmax.jobsearch.model.User;
import com.github.kzhunmax.jobsearch.util.AbstractPostgresTest;
import com.github.kzhunmax.jobsearch.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("JobRepository Tests")
class JobRepositoryTest extends AbstractPostgresTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = TestDataFactory.createUser("user");
        userRepository.save(testUser);

        Job activeJob = TestDataFactory.createJob(testUser, true);
        jobRepository.save(activeJob);


        Job inactiveJob = TestDataFactory.createJob(testUser, false);
        jobRepository.save(inactiveJob);
    }

    @Test
    @DisplayName("Should find all active jobs with pagination")
    void findByActiveTrue_whenJobExists_shouldReturnOnlyActiveJobs() {
        Page<Job> jobs = jobRepository.findByActiveTrue(PageRequest.of(0, 10));

        assertThat(jobs).hasSize(1);
        assertThat(jobs.getContent().getFirst().getTitle()).isEqualTo("Java Dev");
    }

    @Test
    @DisplayName("Should find all active jobs with pagination")
    void findByPostedBy_whenUserExist_shouldReturnAll() {
        Page<Job> jobs = jobRepository.findByPostedBy(testUser, PageRequest.of(0, 10));

        assertThat(jobs).hasSize(2);
    }
}
