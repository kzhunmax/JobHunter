package com.github.kzhunmax.jobsearch.job.repository;

import com.github.kzhunmax.jobsearch.company.model.Company;
import com.github.kzhunmax.jobsearch.company.repository.CompanyRepository;
import com.github.kzhunmax.jobsearch.job.model.Job;
import com.github.kzhunmax.jobsearch.user.model.User;
import com.github.kzhunmax.jobsearch.user.repository.UserRepository;
import com.github.kzhunmax.jobsearch.util.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static com.github.kzhunmax.jobsearch.util.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("JobRepository Tests")
class JobRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private CompanyRepository companyRepository;

    private User testUser;
    private Company testCompany;

    @BeforeEach
    void setUp() {
        testUser = createUser(TEST_EMAIL);
        userRepository.save(testUser);

        testCompany = createCompany(TEST_COMPANY_NAME);
        companyRepository.save(testCompany);
    }

    @Test
    @DisplayName("Should find all active jobs with pagination when active jobs exist")
    void findByActiveTrue_whenActiveJobsExist_shouldReturnOnlyActiveJobs() {
        Job activeJob = createJob(testUser, testCompany, true);
        jobRepository.save(activeJob);

        Job inactiveJob = createJob(testUser, testCompany, false);
        jobRepository.save(inactiveJob);

        Page<Job> jobs = jobRepository.findByActiveTrue(PageRequest.of(0, 10));

        assertThat(jobs.getContent()).hasSize(1);
        assertThat(jobs.getTotalElements()).isEqualTo(1);
        assertThat(jobs.getContent().getFirst().isActive()).isTrue();
    }

    @Test
    @DisplayName("Should return empty page when no active jobs exist")
    void findByActiveTrue_whenNoActiveJobsExist_shouldReturnEmptyPage() {
        Job inactiveJob = createJob(testUser, testCompany, false);
        jobRepository.save(inactiveJob);

        Page<Job> jobs = jobRepository.findByActiveTrue(PageRequest.of(0, 10));

        assertThat(jobs.getContent()).isEmpty();
        assertThat(jobs.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("Should respect pagination for active jobs")
    void findByActiveTrue_withPagination_shouldReturnPaginatedResults() {
        for (int i = 0; i < 5; i++) {
            Job activeJob = createJob(testUser, testCompany, true);
            jobRepository.save(activeJob);
        }

        Pageable pageable = PageRequest.of(0, 3);
        Page<Job> jobsPage1 = jobRepository.findByActiveTrue(pageable);

        assertThat(jobsPage1.getContent()).hasSize(3);
        assertThat(jobsPage1.getTotalElements()).isEqualTo(5);
        assertThat(jobsPage1.getTotalPages()).isEqualTo(2);

        Page<Job> jobsPage2 = jobRepository.findByActiveTrue(PageRequest.of(1, 3));
        assertThat(jobsPage2.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Should find all jobs posted by a user with pagination when jobs exist")
    void findByPostedBy_whenUserHasJobs_shouldReturnAllJobsForUser() {
        Job job1 = createJob(testUser, testCompany, true);
        jobRepository.save(job1);

        Job job2 = createJob(testUser, testCompany, false);
        jobRepository.save(job2);

        Page<Job> jobs = jobRepository.findByPostedById(testUser.getId(), PageRequest.of(0, 10));

        assertThat(jobs.getContent()).hasSize(2);
        assertThat(jobs.getTotalElements()).isEqualTo(2);
        assertThat(jobs.getContent().get(0).getPostedBy()).isEqualTo(testUser);
        assertThat(jobs.getContent().get(1).getPostedBy()).isEqualTo(testUser);
    }

    @Test
    @DisplayName("Should return empty page when user has no jobs")
    void findByPostedBy_whenUserHasNoJobs_shouldReturnEmptyPage() {
        User anotherUser = createUser("anotherUser");
        userRepository.save(anotherUser);

        Page<Job> jobs = jobRepository.findByPostedById(anotherUser.getId(), PageRequest.of(0, 10));

        assertThat(jobs.getContent()).isEmpty();
        assertThat(jobs.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("Should respect pagination for jobs posted by a user")
    void findByPostedBy_withPagination_shouldReturnPaginatedResults() {
        for (int i = 0; i < 5; i++) {
            Job job = createJob(testUser, testCompany, true);
            jobRepository.save(job);
        }

        Pageable pageable = PageRequest.of(0, 3);
        Page<Job> jobsPage1 = jobRepository.findByPostedById(testUser.getId(), pageable);

        assertThat(jobsPage1.getContent()).hasSize(3);
        assertThat(jobsPage1.getTotalElements()).isEqualTo(5);
        assertThat(jobsPage1.getTotalPages()).isEqualTo(2);

        Page<Job> jobsPage2 = jobRepository.findByPostedById(testUser.getId(), PageRequest.of(1, 3));
        assertThat(jobsPage2.getContent()).hasSize(2);
    }
}
