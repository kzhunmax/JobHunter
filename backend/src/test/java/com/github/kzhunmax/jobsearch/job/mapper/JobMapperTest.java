package com.github.kzhunmax.jobsearch.job.mapper;

import com.github.kzhunmax.jobsearch.company.model.Company;
import com.github.kzhunmax.jobsearch.job.dto.JobRequestDTO;
import com.github.kzhunmax.jobsearch.job.dto.JobResponseDTO;
import com.github.kzhunmax.jobsearch.job.model.Job;
import com.github.kzhunmax.jobsearch.shared.RepositoryHelper;
import com.github.kzhunmax.jobsearch.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.github.kzhunmax.jobsearch.util.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JobMapper Tests")
public class JobMapperTest {

    @InjectMocks
    private final JobMapper mapper = Mappers.getMapper(JobMapper.class);

    @Mock
    private RepositoryHelper repositoryHelper;

    private JobRequestDTO validRequestDTO;
    private Job validJob;
    private User testUser;
    private Company testCompany;

    @BeforeEach
    void setUp() {
        testUser = createUser(TEST_ID, TEST_EMAIL);
        testCompany = createCompany(TEST_ID, TEST_COMPANY_NAME);
        validRequestDTO = createJobRequest(TEST_ID);
        validJob = createJob(TEST_ID, testUser, testCompany, true);
    }

    @Test
    @DisplayName("Should map to entity correctly when valid DTO and user provided")
    void toEntity_whenValidDataProvided_shouldMapAllFieldsCorrectly() {
        Job result = mapper.toEntity(validRequestDTO, testUser);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getTitle()).isEqualTo(TEST_TITLE);
        assertThat(result.getDescription()).isEqualTo(TEST_DESCRIPTION);
        assertThat(result.isActive()).isTrue();
        assertThat(result.getPostedBy()).isEqualTo(testUser);
        assertThat(result.getCompany()).isEqualTo(testCompany);
        assertThat(result.getApplications()).isNull();
    }

    @Test
    @DisplayName("Should return null entity when DTO is null")
    void toEntity_whenDtoIsNull_shouldReturnNull() {
        Job result = mapper.toEntity(null, testUser);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should handle null user gracefully (postedBy remains null)")
    void toEntity_whenUserIsNull_shouldHandleNullUserGracefully() {
        Job result = mapper.toEntity(validRequestDTO, null);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo(TEST_TITLE);
        assertThat(result.getDescription()).isEqualTo(TEST_DESCRIPTION);
        assertThat(result.getPostedBy()).isNull();
        assertThat(result.getCompany()).isEqualTo(testCompany);
    }

    @Test
    @DisplayName("Should set company to null when companyId is null or invalid")
    void toEntity_whenCompanyIdIsNull_shouldSetCompanyToNull() {
        JobRequestDTO dtoWithNullCompany = new JobRequestDTO(TEST_TITLE, TEST_DESCRIPTION, null, TEST_LOCATION, TEST_SALARY, FIXED_DEADLINE);

        Job result = mapper.toEntity(dtoWithNullCompany, testUser);

        assertThat(result.getCompany()).isNull();
    }

    @Test
    @DisplayName("Should map to DTO correctly when valid job provided")
    void toDto_whenValidJobProvided_shouldMapAllFieldsCorrectly() {
        JobResponseDTO result = mapper.toDto(validJob);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(TEST_ID);
        assertThat(result.title()).isEqualTo(TEST_TITLE);
        assertThat(result.description()).isEqualTo(TEST_DESCRIPTION);
        assertThat(result.postedBy()).isEqualTo(TEST_EMAIL);
        assertThat(result.company()).isEqualTo(TEST_COMPANY_NAME);
    }

    @Test
    @DisplayName("Should return null DTO when job is null")
    void toDto_whenJobIsNull_shouldReturnNull() {
        JobResponseDTO result = mapper.toDto(null);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should handle null nested fields in job gracefully")
    void toDto_whenJobNestedFieldsAreNull_shouldSetToNull() {
        validJob.setPostedBy(null);
        if (validJob.getCompany() != null) {
            validJob.getCompany().setName(null);
        }

        JobResponseDTO result = mapper.toDto(validJob);

        assertThat(result.postedBy()).isNull();
        assertThat(result.company()).isNull();
    }

    @Test
    @DisplayName("Should update entity correctly from valid DTO")
    void updateEntityFromDto_whenValidDataProvided_shouldUpdateFields() {
        JobRequestDTO updateDTO = updateJobRequest(TEST_ID);

        when(repositoryHelper.findCompanyById(eq(TEST_ID))).thenReturn(testCompany);

        mapper.updateEntityFromDto(updateDTO, validJob);

        assertThat(validJob.getTitle()).isEqualTo("Updated title");
        assertThat(validJob.getDescription()).isEqualTo("Updated description");
        assertThat(validJob.getCompany()).isEqualTo(testCompany);
        assertThat(validJob.isActive()).isTrue();
        assertThat(validJob.getId()).isEqualTo(TEST_ID);
        assertThat(validJob.getPostedBy()).isEqualTo(testUser);
    }

    @Test
    @DisplayName("Should do nothing when DTO is null (no updates)")
    void updateEntityFromDto_whenDtoIsNull_shouldDoNothing() {
        Job originalJob = createJob(TEST_ID + 1, testUser, testCompany, true);
        String originalTitle = originalJob.getTitle();

        mapper.updateEntityFromDto(null, originalJob);

        assertThat(originalJob.getTitle()).isEqualTo(originalTitle);
    }

    @Test
    @DisplayName("Should update company to null when companyId is null in DTO")
    void updateEntityFromDto_whenCompanyIdIsNull_shouldSetCompanyToNull() {
        JobRequestDTO updateDTO = new JobRequestDTO("Updated Title", "Updated Description", null, "Updated Location", TEST_SALARY, FIXED_DEADLINE);

        mapper.updateEntityFromDto(updateDTO, validJob);

        assertThat(validJob.getCompany()).isNull();
    }
}
