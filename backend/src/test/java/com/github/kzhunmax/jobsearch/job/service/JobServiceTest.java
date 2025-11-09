package com.github.kzhunmax.jobsearch.job.service;

import com.github.kzhunmax.jobsearch.company.model.Company;
import com.github.kzhunmax.jobsearch.event.producer.UserEventProducer;
import com.github.kzhunmax.jobsearch.exception.JobNotFoundException;
import com.github.kzhunmax.jobsearch.exception.UserNotFoundException;
import com.github.kzhunmax.jobsearch.job.dto.JobRequestDTO;
import com.github.kzhunmax.jobsearch.job.dto.JobResponseDTO;
import com.github.kzhunmax.jobsearch.job.mapper.JobMapper;
import com.github.kzhunmax.jobsearch.job.model.Job;
import com.github.kzhunmax.jobsearch.job.repository.JobRepository;
import com.github.kzhunmax.jobsearch.shared.RepositoryHelper;
import com.github.kzhunmax.jobsearch.shared.event.JobSyncEvent;
import com.github.kzhunmax.jobsearch.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;

import java.util.List;

import static com.github.kzhunmax.jobsearch.util.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JobService Tests")
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private RepositoryHelper repositoryHelper;

    @Mock
    private JobMapper jobMapper;

    @Mock
    private UserEventProducer eventProducer;

    @Mock
    private PagedResourcesAssembler<JobResponseDTO> pagedAssembler;

    @InjectMocks
    private JobService jobService;

    private User testUser;
    private Job testJob;
    private JobRequestDTO jobRequest;
    private JobResponseDTO expectedResponse;
    private JobRequestDTO updateRequest;
    private JobResponseDTO updatedResponse;
    private Pageable testPageable;

    @BeforeEach
    void setUp() {
        testUser = createUser(TEST_ID, TEST_EMAIL);
        Company testCompany = createCompany(TEST_ID, "BigTech");
        testJob = createJob(TEST_ID, testUser, testCompany, true);

        jobRequest = createJobRequest(testCompany.getId());
        expectedResponse = createJobResponse(testJob.getId(), testCompany.getName(), testUser.getEmail());

        updateRequest = updateJobRequest(testCompany.getId());
        updatedResponse = updateJobResponse(testJob.getId(), "Updated company", testUser.getEmail());

        testPageable = PageRequest.of(0, 10);
    }

    @Nested
    @DisplayName("Create Job tests")
    class CreateJobTests {
        @Test
        @DisplayName("Should create and return a job when user exists")
        void whenUserExists_shouldCreateAndReturnJob() {
            when(repositoryHelper.findUserById(TEST_ID)).thenReturn(testUser);
            when(jobMapper.toEntity(jobRequest, testUser)).thenReturn(testJob);
            when(jobRepository.save(any(Job.class))).thenReturn(testJob);
            doNothing().when(eventProducer).sendJobSyncEvent(any(JobSyncEvent.class));
            when(jobMapper.toDto(testJob)).thenReturn(expectedResponse);

            JobResponseDTO result = jobService.createJob(jobRequest, TEST_ID);

            assertThat(result).isEqualTo(expectedResponse);

            ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
            verify(jobRepository).save(jobCaptor.capture());
            verify(eventProducer).sendJobSyncEvent(any(JobSyncEvent.class));

            Job savedJob = jobCaptor.getValue();
            assertThat(savedJob.getTitle()).isEqualTo(jobRequest.title());
            assertThat(savedJob.getPostedBy()).isEqualTo(testUser);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user does not exist")
        void whenUserNotFound_shouldThrowException() {
            when(repositoryHelper.findUserById(NON_EXISTENT_ID)).thenThrow(new UserNotFoundException(NON_EXISTENT_ID));

            assertThatThrownBy(() -> jobService.createJob(jobRequest, NON_EXISTENT_ID))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessage("User with id " + NON_EXISTENT_ID + " not found");

            verify(repositoryHelper).findUserById(NON_EXISTENT_ID);
            verify(jobRepository, never()).save(any(Job.class));
        }
    }

    @Nested
    @DisplayName("Find Job By ID Tests")
    class FindJobByIdTests {
        @Test
        @DisplayName("Should return id of job when job exists")
        void whenJobExists_shouldReturnJob() {
            // Fixed: Mock RepositoryHelper and JobMapper
            when(repositoryHelper.findJobById(TEST_ID)).thenReturn(testJob);
            when(jobMapper.toDto(testJob)).thenReturn(expectedResponse);

            JobResponseDTO result = jobService.getJobById(TEST_ID);

            assertThat(result).isEqualTo(expectedResponse);
            verify(repositoryHelper).findJobById(TEST_ID);
            verify(jobMapper).toDto(testJob);
        }

        @Test
        @DisplayName("Should throw JobNotFoundException when job does not exist")
        void whenJobNotFound_shouldThrowException() {
            // Fixed: Mock RepositoryHelper
            when(repositoryHelper.findJobById(NON_EXISTENT_ID)).thenThrow(new JobNotFoundException(NON_EXISTENT_ID));

            assertThatThrownBy(() -> jobService.getJobById(NON_EXISTENT_ID))
                    .isInstanceOf(JobNotFoundException.class)
                    .hasMessage(String.format(JOB_NOT_FOUND_MESSAGE, NON_EXISTENT_ID));

            verify(repositoryHelper).findJobById(NON_EXISTENT_ID);
            verifyNoMoreInteractions(jobRepository);
        }
    }

    @Nested
    @DisplayName("Update Job Tests")
    class UpdateJobTests {
        @Test
        @DisplayName("Should update and return new job details when job exists")
        void whenJobExists_shouldReturnUpdatedJobDetails() {
            when(repositoryHelper.findJobById(TEST_ID)).thenReturn(testJob);
            doNothing().when(jobMapper).updateEntityFromDto(eq(updateRequest), eq(testJob));
            when(jobRepository.save(any(Job.class))).thenReturn(testJob);
            doNothing().when(eventProducer).sendJobSyncEvent(any(JobSyncEvent.class));
            when(jobMapper.toDto(testJob)).thenReturn(updatedResponse);


            JobResponseDTO result = jobService.updateJob(TEST_ID, updateRequest);

            ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
            verify(jobRepository).save(jobCaptor.capture());
            verify(jobMapper).updateEntityFromDto(updateRequest, testJob);
            verify(eventProducer).sendJobSyncEvent(any(JobSyncEvent.class));

            assertThat(result).isEqualTo(updatedResponse);
        }

        @Test
        @DisplayName("Should throw JobNotFoundException when job does not exist")
        void whenJobNotFound_shouldThrowException() {
            // Fixed: Mock RepositoryHelper
            when(repositoryHelper.findJobById(NON_EXISTENT_ID)).thenThrow(new JobNotFoundException(NON_EXISTENT_ID));

            assertThatThrownBy(() -> jobService.updateJob(NON_EXISTENT_ID, jobRequest))
                    .isInstanceOf(JobNotFoundException.class)
                    .hasMessage(String.format(JOB_NOT_FOUND_MESSAGE, NON_EXISTENT_ID));

            verify(repositoryHelper).findJobById(NON_EXISTENT_ID);
            verify(jobRepository, never()).save(any(Job.class));
        }
    }

    @Nested
    @DisplayName("Delete Job Tests")
    class DeleteJobTest {
        @Test
        @DisplayName("Should deactivate job when job exists")
        void whenJobExists_shouldDeactivateAndSave() {
            when(repositoryHelper.findJobById(TEST_ID)).thenReturn(testJob);
            when(jobRepository.save(any(Job.class))).thenReturn(testJob);
            doNothing().when(eventProducer).sendJobSyncEvent(any(JobSyncEvent.class));

            jobService.deleteJob(TEST_ID);

            ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
            verify(jobRepository).save(jobCaptor.capture());
            verify(eventProducer).sendJobSyncEvent(any(JobSyncEvent.class));

            Job savedJob = jobCaptor.getValue();
            assertThat(savedJob.isActive()).isFalse();
            verify(repositoryHelper).findJobById(TEST_ID);
        }

        @Test
        @DisplayName("Should throw JobNotFoundException when job does not exist")
        void whenJobNotFound_shouldThrowException() {
            when(repositoryHelper.findJobById(NON_EXISTENT_ID)).thenThrow(new JobNotFoundException(NON_EXISTENT_ID));

            assertThatThrownBy(() -> jobService.deleteJob(NON_EXISTENT_ID))
                    .isInstanceOf(JobNotFoundException.class);

            verify(repositoryHelper).findJobById(NON_EXISTENT_ID);
            verify(jobRepository, never()).save(any(Job.class));
        }
    }

    @Nested
    @DisplayName("Get All Active Jobs Tests")
    class GetAllActiveJobsTests {
        @Test
        @DisplayName("Should return paged model with jobs when active job exist")
        void whenJobExists_shouldReturnPagedJobs() {
            // Arrange
            Page<Job> jobPage = new PageImpl<>(List.of(testJob), testPageable, 1);
            PagedModel<EntityModel<JobResponseDTO>> expectedModel = PagedModel.of(
                    List.of(EntityModel.of(expectedResponse)),
                    new PagedModel.PageMetadata(1, 0, 1)
            );

            when(jobRepository.findByActiveTrue(testPageable)).thenReturn(jobPage);

            when(jobMapper.toDto(testJob)).thenReturn(expectedResponse);

            when(pagedAssembler.toModel(any(Page.class), any(RepresentationModelAssembler.class)))
                    .thenReturn(expectedModel);

            // Act
            PagedModel<EntityModel<JobResponseDTO>> result = jobService.getAllActiveJobs(testPageable, pagedAssembler);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().iterator().next().getContent()).isEqualTo(expectedResponse);

            verify(jobRepository).findByActiveTrue(testPageable);
            verify(jobMapper).toDto(testJob); // Verify the mapper was called
            verify(pagedAssembler).toModel(any(Page.class), any(RepresentationModelAssembler.class));
        }

        @Test
        @DisplayName("Should return empty paged model when no active jobs exist")
        void whenJobsNotExists_shouldReturnEmptyPage() {
            // Arrange
            Page<Job> emptyJobPage = Page.empty(testPageable); // Use the correct <Job> type
            PagedModel<EntityModel<JobResponseDTO>> emptyModel = PagedModel.of(
                    List.of(),
                    new PagedModel.PageMetadata(0, 0, 0)
            );

            when(jobRepository.findByActiveTrue(testPageable)).thenReturn(emptyJobPage);

            when(pagedAssembler.toModel(any(Page.class), any(RepresentationModelAssembler.class)))
                    .thenReturn(emptyModel);

            // Act
            PagedModel<EntityModel<JobResponseDTO>> result = jobService.getAllActiveJobs(testPageable, pagedAssembler);

            // Assert
            assertThat(result.getContent()).isEmpty();
            verify(jobRepository).findByActiveTrue(testPageable);
            verify(jobMapper, never()).toDto(any()); // Verify mapper was NOT called
        }
    }

    @Nested
    @DisplayName("Get Job By Recruiter Tests")
    class GetJobByRecruiterTests {
        @Test
        @DisplayName("Should return paged jobs for recruiter when recruiter exist ")
        void whenRecruiterExists_shouldReturnPagedJobs() {
            // Arrange
            Page<Job> jobPage = new PageImpl<>(List.of(testJob), testPageable, 1);
            PagedModel<EntityModel<JobResponseDTO>> expectedModel = PagedModel.of(
                    List.of(EntityModel.of(expectedResponse)),
                    new PagedModel.PageMetadata(1, 0, 1)
            );

            when(jobRepository.findByPostedById(TEST_ID, testPageable)).thenReturn(jobPage);
            when(jobMapper.toDto(testJob)).thenReturn(expectedResponse); // <--- The fix
            when(pagedAssembler.toModel(any(Page.class), any(RepresentationModelAssembler.class)))
                    .thenReturn(expectedModel);

            // Act
            PagedModel<EntityModel<JobResponseDTO>> result = jobService.getJobsByRecruiter(TEST_ID, testPageable, pagedAssembler);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().iterator().next().getContent()).isEqualTo(expectedResponse);

            verify(jobRepository).findByPostedById(TEST_ID, testPageable);
            verify(jobMapper).toDto(testJob);
        }


        @Test
        @DisplayName("Should handle empty job list for recruiter")
        void whenNoJobs_shouldReturnEmptyPage() {
            // Arrange
            Page<Job> emptyJobPage = Page.empty(testPageable); // Use the correct <Job> type
            PagedModel<EntityModel<JobResponseDTO>> emptyModel = PagedModel.of(
                    List.of(),
                    new PagedModel.PageMetadata(0, 0, 0)
            );

            when(jobRepository.findByPostedById(TEST_ID, testPageable)).thenReturn(emptyJobPage);
            when(pagedAssembler.toModel(any(Page.class), any(RepresentationModelAssembler.class)))
                    .thenReturn(emptyModel);

            // Act
            PagedModel<EntityModel<JobResponseDTO>> result = jobService.getJobsByRecruiter(TEST_ID, testPageable, pagedAssembler);

            // Assert
            assertThat(result.getContent()).isEmpty();
            verify(jobRepository).findByPostedById(TEST_ID, testPageable);
            verify(jobMapper, never()).toDto(any());
        }
    }
}