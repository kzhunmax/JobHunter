package com.github.kzhunmax.jobsearch.job.mapper;

import com.github.kzhunmax.jobsearch.company.model.Company;
import com.github.kzhunmax.jobsearch.job.dto.JobApplicationResponseDTO;
import com.github.kzhunmax.jobsearch.job.model.Job;
import com.github.kzhunmax.jobsearch.job.model.JobApplication;
import com.github.kzhunmax.jobsearch.user.model.Resume;
import com.github.kzhunmax.jobsearch.user.model.User;
import com.github.kzhunmax.jobsearch.user.model.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.github.kzhunmax.jobsearch.util.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("JobApplicationMapper Tests")
class JobApplicationMapperTest {

    @InjectMocks
    private JobApplicationMapper mapper = Mappers.getMapper(JobApplicationMapper.class);

    private JobApplication application;

    @BeforeEach
    void setUp() {
        User user = createUser(TEST_ID, TEST_EMAIL);
        Company company = createCompany(TEST_ID, TEST_COMPANY_NAME);
        UserProfile profile = createUserProfile(user);
        user.setProfile(profile);
        Resume resume = createResume(TEST_ID, profile);

        Job job = createJob(TEST_ID, user, company, true);
        application = createJobApplication(user, job, resume);
    }

    @Test
    @DisplayName("Should map all fields correctly when valid data is provided")
    void toDto_whenValidDataProvided_shouldMapAllFieldsCorrectly() {
        JobApplicationResponseDTO expected = createJobApplicationResponseDTO(application);

        JobApplicationResponseDTO result = mapper.toDto(application);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should return null when application is null")
    void toDto_whenApplicationIsNull_shouldReturnNull() {
        JobApplicationResponseDTO result = mapper.toDto(null);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should handle null job gracefully and set job-related fields to null")
    void toDto_whenJobIsNull_shouldHandleNullJobGracefully() {
        application.setJob(null);

        JobApplicationResponseDTO result = mapper.toDto(application);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(application.getId());
        assertThat(result.jobId()).isNull();
        assertThat(result.jobTitle()).isNull();
        assertThat(result.company()).isNull();
        assertThat(result.candidateEmail()).isEqualTo(application.getCandidate().getEmail());
        assertThat(result.candidateProfileId()).isEqualTo(application.getCandidate().getProfile().getId());
        assertThat(result.status()).isEqualTo(application.getStatus().name());
        assertThat(result.appliedAt()).isEqualTo(application.getAppliedAt().toString());
        assertThat(result.coverLetter()).isEqualTo(application.getCoverLetter());
        assertThat(result.resumeUrl()).isEqualTo(application.getResume().getFileUrl());
    }

    @Test
    @DisplayName("Should handle null candidate gracefully and set candidate-related fields to null")
    void toDto_whenCandidateIsNull_shouldHandleNullCandidateGracefully() {
        application.setCandidate(null);

        JobApplicationResponseDTO result = mapper.toDto(application);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(application.getId());
        assertThat(result.jobId()).isEqualTo(application.getJob().getId());
        assertThat(result.jobTitle()).isEqualTo(application.getJob().getTitle());
        assertThat(result.company()).isEqualTo(application.getJob().getCompany().getName());
        assertThat(result.candidateEmail()).isNull();
        assertThat(result.candidateProfileId()).isNull();
        assertThat(result.status()).isEqualTo(application.getStatus().name());
        assertThat(result.appliedAt()).isEqualTo(application.getAppliedAt().toString());
        assertThat(result.coverLetter()).isEqualTo(application.getCoverLetter());
        assertThat(result.resumeUrl()).isEqualTo(application.getResume().getFileUrl());
    }

    @Test
    @DisplayName("Should handle null status and appliedAt gracefully")
    void toDto_whenStatusOrAppliedAtIsNull_shouldSetToNull() {
        application.setStatus(null);
        application.setAppliedAt(null);

        JobApplicationResponseDTO result = mapper.toDto(application);

        assertThat(result).isNotNull();
        assertThat(result.status()).isNull();
        assertThat(result.appliedAt()).isNull();
    }

    @Test
    @DisplayName("Should handle null resume gracefully and set resumeUrl to null")
    void toDto_whenResumeIsNull_shouldSetResumeUrlToNull() {
        application.setResume(null);

        JobApplicationResponseDTO result = mapper.toDto(application);

        assertThat(result.resumeUrl()).isNull();
    }
}