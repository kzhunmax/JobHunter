package com.github.kzhunmax.jobsearch.mapper;

import com.github.kzhunmax.jobsearch.dto.response.JobApplicationResponseDTO;
import com.github.kzhunmax.jobsearch.model.Job;
import com.github.kzhunmax.jobsearch.model.JobApplication;
import com.github.kzhunmax.jobsearch.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.kzhunmax.jobsearch.util.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JobApplicationMapper Tests")
public class JobApplicationMapperTest {

    private JobApplicationMapper mapper;
    private JobApplication application;

    @BeforeEach
    void setUp() {
        User user = createUser(TEST_ID, TEST_USERNAME);
        Job job = createJob(TEST_ID, user, true);
        application = createJobApplication(user, job);
        mapper = new JobApplicationMapper();
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
        assertThat(result.candidateUsername()).isEqualTo(application.getCandidate().getUsername());
        assertThat(result.status()).isEqualTo(application.getStatus().name());
        assertThat(result.appliedAt()).isEqualTo(application.getAppliedAt().toString());
        assertThat(result.coverLetter()).isEqualTo(application.getCoverLetter());
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
        assertThat(result.company()).isEqualTo(application.getJob().getCompany());
        assertThat(result.candidateUsername()).isNull();
        assertThat(result.status()).isEqualTo(application.getStatus().name());
        assertThat(result.appliedAt()).isEqualTo(application.getAppliedAt().toString());
        assertThat(result.coverLetter()).isEqualTo(application.getCoverLetter());
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
}
