package com.github.kzhunmax.jobsearch.repository;

import com.github.kzhunmax.jobsearch.model.Job;
import com.github.kzhunmax.jobsearch.model.JobApplication;
import com.github.kzhunmax.jobsearch.model.User;
import com.github.kzhunmax.jobsearch.util.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static com.github.kzhunmax.jobsearch.util.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("JobApplicationRepository Tests")
public class JobApplicationRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    private User testUser;
    private Job testJob;

    @BeforeEach
    void setUp() {
        testUser = createUser(TEST_USERNAME);
        userRepository.save(testUser);

        testJob = createJob(testUser, true);
        jobRepository.save(testJob);
    }

    @Test
    @DisplayName("Should return applications for a job when applications exist")
    void findByJob_whenApplicationsExist_shouldReturnApplicationsForJob() {
        JobApplication application = createJobApplication(testUser, testJob);
        jobApplicationRepository.save(application);

        Page<JobApplication> result = jobApplicationRepository.findByJob(testJob, Pageable.unpaged());

        assertThat(result).isNotEmpty();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getJob().getId()).isEqualTo(testJob.getId());
        assertThat(result.getContent().getFirst().getCandidate().getId()).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("Should return empty page when no applications exist for a job")
    void findByJob_whenNoApplicationsExist_shouldReturnEmptyPage() {
        Page<JobApplication> result = jobApplicationRepository.findByJob(testJob, Pageable.unpaged());

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("Should respect pagination for applications by job")
    void findByJob_withPagination_shouldReturnPaginatedResults() {
        for (int i = 0; i < 5; i++) {
            User candidate = createUser("candidate" + i);
            userRepository.save(candidate);
            JobApplication application = createJobApplication(candidate, testJob);
            jobApplicationRepository.save(application);
        }

        Page<JobApplication> page1 = jobApplicationRepository.findByJob(testJob, PageRequest.of(0, 3));

        assertThat(page1.getContent()).hasSize(3);
        assertThat(page1.getTotalElements()).isEqualTo(5);
        assertThat(page1.getTotalPages()).isEqualTo(2);

        Page<JobApplication> page2 = jobApplicationRepository.findByJob(testJob, PageRequest.of(1, 3));
        assertThat(page2.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Should return applications for a candidate when applications exist")
    void findByCandidate_whenApplicationsExist_shouldReturnApplicationsForCandidate() {
        JobApplication application = createJobApplication(testUser, testJob);
        jobApplicationRepository.save(application);

        Page<JobApplication> result = jobApplicationRepository.findByCandidate(testUser, Pageable.unpaged());

        assertThat(result).isNotEmpty();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getCandidate().getUsername()).isEqualTo(testUser.getUsername());
        assertThat(result.getContent().getFirst().getJob().getId()).isEqualTo(testJob.getId());
    }

    @Test
    @DisplayName("Should return empty page when no applications exist for a candidate")
    void findByCandidate_whenNoApplicationsExist_shouldReturnEmptyPage() {
        User anotherUser = createUser("anotherUser");
        userRepository.save(anotherUser);

        Page<JobApplication> result = jobApplicationRepository.findByCandidate(anotherUser, Pageable.unpaged());

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("Should return job application when application exist")
    void findByJobAndCandidate_whenApplicationExist_shouldReturnJobApplication() {
        JobApplication application = createJobApplication(testUser, testJob);
        jobApplicationRepository.save(application);

        Optional<JobApplication> result = jobApplicationRepository.findByJobAndCandidate(testJob, testUser);

        assertThat(result).isPresent();
        assertThat(result.get().getJob().getId()).isEqualTo(testJob.getId());
        assertThat(result.get().getCandidate().getId()).isEqualTo(testUser.getId());
        assertThat(result.get().getStatus().name()).isEqualTo("APPLIED");
    }

    @Test
    @DisplayName("Should respect pagination for applications by candidate")
    void findByCandidate_withPagination_shouldReturnPaginatedResults() {
        for (int i = 0; i < 5; i++) {
            Job job = createJob(testUser, true);
            jobRepository.save(job);
            JobApplication application = createJobApplication(testUser, job);
            jobApplicationRepository.save(application);
        }

        Page<JobApplication> page1 = jobApplicationRepository.findByCandidate(testUser, PageRequest.of(0, 3));

        assertThat(page1.getContent()).hasSize(3);
        assertThat(page1.getTotalElements()).isEqualTo(5);
        assertThat(page1.getTotalPages()).isEqualTo(2);

        Page<JobApplication> page2 = jobApplicationRepository.findByCandidate(testUser, PageRequest.of(1, 3));
        assertThat(page2.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Should return job application when application exists")
    void findByJobAndCandidate_whenApplicationExists_shouldReturnJobApplication() {
        JobApplication application = createJobApplication(testUser, testJob);
        jobApplicationRepository.save(application);

        Optional<JobApplication> result = jobApplicationRepository.findByJobAndCandidate(testJob, testUser);

        assertThat(result).isPresent();
        assertThat(result.get().getJob().getId()).isEqualTo(testJob.getId());
        assertThat(result.get().getCandidate().getId()).isEqualTo(testUser.getId());
        assertThat(result.get().getStatus().name()).isEqualTo("APPLIED");
    }

    @Test
    @DisplayName("Should return empty optional when application does not exist")
    void findByJobAndCandidate_whenNotExists_shouldReturnEmpty() {
        Optional<JobApplication> result = jobApplicationRepository.findByJobAndCandidate(testJob, testUser);

        assertThat(result).isEmpty();
    }
}
