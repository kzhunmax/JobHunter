package com.github.kzhunmax.jobsearch.security;

import com.github.kzhunmax.jobsearch.exception.ApplicationNotFoundException;
import com.github.kzhunmax.jobsearch.exception.JobNotFoundException;
import com.github.kzhunmax.jobsearch.model.ApplicationStatus;
import com.github.kzhunmax.jobsearch.model.Job;
import com.github.kzhunmax.jobsearch.model.JobApplication;
import com.github.kzhunmax.jobsearch.model.User;
import com.github.kzhunmax.jobsearch.repository.JobApplicationRepository;
import com.github.kzhunmax.jobsearch.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Optional;
import java.util.Set;

import static com.github.kzhunmax.jobsearch.util.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JobSecurityServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @InjectMocks
    private JobSecurityService jobSecurityService;

    private Authentication authentication;
    private Authentication adminAuth;
    private Job job;
    private JobApplication application;
    private static final String OTHER_USER = "otheruser";

    @BeforeEach
    void setUp() {
        User owner = createUser(TEST_USERNAME);
        User candidate = createUser(TEST_USERNAME);
        job = createJob(TEST_ID, owner, true);
        application = createJobApplication(TEST_ID, candidate, job);

        authentication = new UsernamePasswordAuthenticationToken(TEST_USERNAME, null, Set.of(new SimpleGrantedAuthority("ROLE_CANDIDATE")));
        adminAuth = new UsernamePasswordAuthenticationToken(TEST_USERNAME, null, Set.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void isJobOwner_authenticatedOwner_returnsTrue() {
        when(jobRepository.findById(TEST_ID)).thenReturn(Optional.of(job));

        boolean result = jobSecurityService.isJobOwner(TEST_ID, authentication);

        assertThat(result).isTrue();
        verify(jobRepository).findById(TEST_ID);
    }

    @Test
    void isJobOwner_notOwner_returnsFalse() {
        User otherOwner = User.builder().username(OTHER_USER).build();
        Job otherJob = Job.builder().id(TEST_ID).postedBy(otherOwner).build();
        when(jobRepository.findById(TEST_ID)).thenReturn(Optional.of(otherJob));

        boolean result = jobSecurityService.isJobOwner(TEST_ID, authentication);

        assertThat(result).isFalse();
    }

    @Test
    void isJobOwner_notAuthenticated_returnsFalse() {
        boolean result = jobSecurityService.isJobOwner(TEST_ID, null);

        assertThat(result).isFalse();
        verify(jobRepository, never()).findById(any());
    }

    @Test
    void isJobOwner_jobNotFound_throwsException() {
        when(jobRepository.findById(TEST_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobSecurityService.isJobOwner(TEST_ID, authentication))
                .isInstanceOf(JobNotFoundException.class);
    }

    @Test
    void canUpdateApplication_isAdmin_returnsTrue() {
        when(jobApplicationRepository.findById(TEST_ID)).thenReturn(Optional.of(application));

        boolean result = jobSecurityService.canUpdateApplication(TEST_ID, ApplicationStatus.REJECTED, adminAuth);

        assertThat(result).isTrue();
    }

    @Test
    void canUpdateApplication_jobOwner_returnsTrue() {
        User owner = User.builder().username(TEST_USERNAME).build();
        Job ownedJob = Job.builder().postedBy(owner).build();
        JobApplication ownedApp = JobApplication.builder().job(ownedJob).build();
        when(jobApplicationRepository.findById(TEST_ID)).thenReturn(Optional.of(ownedApp));

        boolean result = jobSecurityService.canUpdateApplication(TEST_ID, ApplicationStatus.REJECTED, authentication);

        assertThat(result).isTrue();
    }

    @Test
    void canUpdateApplication_candidateRejectStatus_returnsTrue() {
        User candidate = User.builder().username(OTHER_USER).build();
        application.setCandidate(candidate);
        when(jobApplicationRepository.findById(TEST_ID)).thenReturn(Optional.of(application));

        boolean result = jobSecurityService.canUpdateApplication(TEST_ID, ApplicationStatus.REJECTED,
                new UsernamePasswordAuthenticationToken(OTHER_USER, null, Set.of(new SimpleGrantedAuthority("ROLE_CANDIDATE"))));

        assertThat(result).isTrue();
    }

    @Test
    void canUpdateApplication_candidateOtherStatus_returnsFalse() {
        when(jobApplicationRepository.findById(TEST_ID)).thenReturn(Optional.of(application));

        boolean result = jobSecurityService.canUpdateApplication(TEST_ID, ApplicationStatus.ACCEPTED,
                new UsernamePasswordAuthenticationToken(OTHER_USER, null, Set.of(new SimpleGrantedAuthority("ROLE_CANDIDATE"))));

        assertThat(result).isFalse();
    }

    @Test
    void canUpdateApplication_notAuthenticated_returnsFalse() {
        boolean result = jobSecurityService.canUpdateApplication(TEST_ID, ApplicationStatus.REJECTED, null);

        assertThat(result).isFalse();
        verify(jobApplicationRepository, never()).findById(any());
    }

    @Test
    void canUpdateApplication_applicationNotFound_throwsException() {
        when(jobApplicationRepository.findById(TEST_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobSecurityService.canUpdateApplication(TEST_ID, ApplicationStatus.REJECTED, authentication))
                .isInstanceOf(ApplicationNotFoundException.class);
    }
}
