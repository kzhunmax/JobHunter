package com.github.kzhunmax.jobsearch.security;

import com.github.kzhunmax.jobsearch.company.model.Company;
import com.github.kzhunmax.jobsearch.job.model.Job;
import com.github.kzhunmax.jobsearch.job.model.JobApplication;
import com.github.kzhunmax.jobsearch.shared.RepositoryHelper;
import com.github.kzhunmax.jobsearch.shared.enums.ApplicationStatus;
import com.github.kzhunmax.jobsearch.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;

import static com.github.kzhunmax.jobsearch.util.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JobSecurityService Unit Tests")
public class JobSecurityServiceTest {

    @Mock
    private RepositoryHelper repositoryHelper;

    @InjectMocks
    private JobSecurityService jobSecurityService;

    // Arrange
    private Job job;
    private JobApplication application;

    private Authentication ownerAuth;
    private Authentication candidateAuth;
    private Authentication adminAuth;
    private Authentication otherAuth;

    private static final String CANDIDATE_EMAIL = "candidate@example.com";
    private static final String OTHER_EMAIL = "other@example.com";
    private static final String ADMIN_EMAIL = "admin@example.com";

    @BeforeEach
    void setUp() {
        User owner = createUser(1L, TEST_EMAIL);
        User candidate = createUser(2L, CANDIDATE_EMAIL);
        Company company = createCompany(1L, "Test Co");
        job = createJob(TEST_ID, owner, company, true);
        application = createJobApplication(TEST_ID, candidate, job, null);

        ownerAuth = new UsernamePasswordAuthenticationToken(TEST_EMAIL, null, Set.of(new SimpleGrantedAuthority("ROLE_RECRUITER")));
        candidateAuth = new UsernamePasswordAuthenticationToken(CANDIDATE_EMAIL, null, Set.of(new SimpleGrantedAuthority("ROLE_CANDIDATE")));
        adminAuth = new UsernamePasswordAuthenticationToken(ADMIN_EMAIL, null, Set.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        otherAuth = new UsernamePasswordAuthenticationToken(OTHER_EMAIL, null, Set.of(new SimpleGrantedAuthority("ROLE_CANDIDATE")));
    }

    @Test
    @DisplayName("isJobOwner should return true when user is the owner")
    void isJobOwner_authenticatedOwner_returnsTrue() {
        when(repositoryHelper.findJobById(TEST_ID)).thenReturn(job);

        boolean result = jobSecurityService.isJobOwner(TEST_ID, ownerAuth);

        assertThat(result).isTrue();
        verify(repositoryHelper).findJobById(TEST_ID);
    }

    @Test
    @DisplayName("isJobOwner should return false when user is not the owner")
    void isJobOwner_shouldReturnFalse_whenUserIsNotOwner() {
        when(repositoryHelper.findJobById(TEST_ID)).thenReturn(job);

        boolean result = jobSecurityService.isJobOwner(TEST_ID, otherAuth);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isJobOwner should return false when user is not authenticated")
    void isJobOwner_shouldReturnFalse_whenNotAuthenticated() {
        // Act
        boolean result = jobSecurityService.isJobOwner(TEST_ID, null);

        // Assert
        assertThat(result).isFalse();
        verify(repositoryHelper, never()).findJobById(any());
    }

    @Test
    @DisplayName("canUpdateApplication should return true for admin")
    void canUpdateApplication_shouldReturnTrue_forAdmin() {
        when(repositoryHelper.findApplicationById(TEST_ID)).thenReturn(application);

        boolean result = jobSecurityService.canUpdateApplication(TEST_ID, ApplicationStatus.REJECTED, adminAuth);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("canUpdateApplication should return true for job owner")
    void canUpdateApplication_shouldReturnTrue_forJobOwner() {
        when(repositoryHelper.findApplicationById(TEST_ID)).thenReturn(application);

        boolean result = jobSecurityService.canUpdateApplication(TEST_ID, ApplicationStatus.ACCEPTED, ownerAuth);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("canUpdateApplication should return true for candidate setting status to REJECTED")
    void canUpdateApplication_shouldReturnTrue_forCandidateRejecting() {
        when(repositoryHelper.findApplicationById(TEST_ID)).thenReturn(application);

        boolean result = jobSecurityService.canUpdateApplication(TEST_ID, ApplicationStatus.REJECTED, candidateAuth);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("canUpdateApplication should return false for candidate setting other status")
    void canUpdateApplication_shouldReturnFalse_forCandidateAccepting() {
        when(repositoryHelper.findApplicationById(TEST_ID)).thenReturn(application);

        boolean result = jobSecurityService.canUpdateApplication(TEST_ID, ApplicationStatus.ACCEPTED, candidateAuth);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("canUpdateApplication should return false for unrelated user")
    void canUpdateApplication_shouldReturnFalse_forOtherUser() {
        // Arrange
        when(repositoryHelper.findApplicationById(TEST_ID)).thenReturn(application);

        // Act
        boolean result = jobSecurityService.canUpdateApplication(TEST_ID, ApplicationStatus.REJECTED, otherAuth);

        // Assert
        assertThat(result).isFalse();
    }
}
