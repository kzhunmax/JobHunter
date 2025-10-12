package com.github.kzhunmax.jobsearch.service;

import com.github.kzhunmax.jobsearch.dto.request.JobRequestDTO;
import com.github.kzhunmax.jobsearch.dto.response.JobResponseDTO;
import com.github.kzhunmax.jobsearch.exception.JobNotFoundException;
import com.github.kzhunmax.jobsearch.model.Job;
import com.github.kzhunmax.jobsearch.model.User;
import com.github.kzhunmax.jobsearch.repository.JobRepository;
import com.github.kzhunmax.jobsearch.repository.UserRepository;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static com.github.kzhunmax.jobsearch.util.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JobService Tests")
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PagedResourcesAssembler<JobResponseDTO> pagedAssembler;

    @InjectMocks
    private JobService jobService;

    private User testUser;
    private Job testJob;
    private JobRequestDTO jobRequest;
    private JobResponseDTO expectedResponse;
    private JobRequestDTO updateRequest;
    private Pageable testPageable;
    private Page<Job> emptyPage;

    @BeforeEach
    void setUp() {
        testUser = createUser(TEST_ID, TEST_USERNAME);
        testJob = createJob(TEST_ID, testUser, true);
        jobRequest = createJobRequest();
        expectedResponse = createJobResponse(TEST_ID);
        updateRequest = updateJobRequest();
        testPageable = PageRequest.of(0, 10);
        emptyPage = Page.empty(testPageable);
    }

    @Nested
    @DisplayName("Create Job tests")
    class CreateJobTests {
        @Test
        @DisplayName("Should create and return a job when user exists")
        void whenUserExists_shouldCreateAndReturnJob() {
            when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
            when(jobRepository.save(any(Job.class))).thenReturn(testJob);

            JobResponseDTO result = jobService.createJob(jobRequest, TEST_USERNAME);

            assertThat(result)
                    .returns(expectedResponse.id(), JobResponseDTO::id)
                    .returns(expectedResponse.title(), JobResponseDTO::title)
                    .returns(expectedResponse.description(), JobResponseDTO::description)
                    .returns(expectedResponse.company(), JobResponseDTO::company)
                    .returns(expectedResponse.location(), JobResponseDTO::location)
                    .returns(expectedResponse.salary(), JobResponseDTO::salary)
                    .returns(expectedResponse.active(), JobResponseDTO::active)
                    .returns(expectedResponse.postedBy(), JobResponseDTO::postedBy);

            ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
            verify(jobRepository).save(jobCaptor.capture());

            Job savedJob = jobCaptor.getValue();
            assertThat(savedJob.getTitle()).isEqualTo(jobRequest.title());
            assertThat(savedJob.getDescription()).isEqualTo(jobRequest.description());
            assertThat(savedJob.getCompany()).isEqualTo(jobRequest.company());
            assertThat(savedJob.getLocation()).isEqualTo(jobRequest.location());
            assertThat(savedJob.getSalary()).isEqualTo(jobRequest.salary());
            assertThat(savedJob.getPostedBy()).isEqualTo(testUser);

            verifyNoMoreInteractions(jobRepository, userRepository);
        }

        @Test
        @DisplayName("Should throw UsernameNotFoundException when user does not exist")
        void whenUserNotFound_shouldThrowException() {
            when(userRepository.findByUsername(NON_EXISTENT_USERNAME)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> jobService.createJob(jobRequest, NON_EXISTENT_USERNAME))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessage(USER_NOT_FOUND_MESSAGE + NON_EXISTENT_USERNAME);

            verify(userRepository).findByUsername(NON_EXISTENT_USERNAME);
            verify(jobRepository, never()).save(any(Job.class));
            verifyNoMoreInteractions(jobRepository, userRepository);
        }
    }

    @Nested
    @DisplayName("Find Job By ID Tests")
    class FindJobByIdTests {
        @Test
        @DisplayName("Should return id of job when job exists")
        void whenJobExists_shouldReturnJob() {
            when(jobRepository.findById(TEST_ID)).thenReturn(Optional.of(testJob));

            JobResponseDTO result = jobService.getJobById(TEST_ID);

            assertThat(result)
                    .returns(expectedResponse.id(), JobResponseDTO::id)
                    .returns(expectedResponse.title(), JobResponseDTO::title)
                    .returns(expectedResponse.description(), JobResponseDTO::description)
                    .returns(expectedResponse.company(), JobResponseDTO::company)
                    .returns(expectedResponse.location(), JobResponseDTO::location)
                    .returns(expectedResponse.salary(), JobResponseDTO::salary)
                    .returns(expectedResponse.active(), JobResponseDTO::active)
                    .returns(expectedResponse.postedBy(), JobResponseDTO::postedBy);
        }

        @Test
        @DisplayName("Should throw JobNotFoundException when job does not exist")
        void whenJobNotFound_shouldThrowException() {
            when(jobRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> jobService.getJobById(NON_EXISTENT_ID))
                    .isInstanceOf(JobNotFoundException.class)
                    .hasMessage(String.format(JOB_NOT_FOUND_MESSAGE, NON_EXISTENT_ID));

            verify(jobRepository).findById(NON_EXISTENT_ID);
            verifyNoMoreInteractions(jobRepository);
        }
    }

    @Nested
    @DisplayName("Update Job Tests")
    class UpdateJobTests {
        @Test
        @DisplayName("Should update and return new job details when job exists")
        void whenJobExists_shouldReturnUpdatedJobDetails() {
            when(jobRepository.findById(TEST_ID)).thenReturn(Optional.of(testJob));
            when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

            JobResponseDTO result = jobService.updateJob(TEST_ID, updateRequest);

            ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
            verify(jobRepository).save(jobCaptor.capture());

            Job capturedJob = jobCaptor.getValue();
            assertThat(capturedJob.getTitle()).isEqualTo(updateRequest.title());
            assertThat(capturedJob.getSalary()).isEqualTo(updateRequest.salary());

            assertThat(result)
                    .returns("Updated title", JobResponseDTO::title)
                    .returns("Updated description", JobResponseDTO::description)
                    .returns("Updated company", JobResponseDTO::company)
                    .returns("Updated location", JobResponseDTO::location)
                    .returns(5000.0, JobResponseDTO::salary);
        }

        @Test
        @DisplayName("Should throw JobNotFoundException when job does not exist")
        void whenJobNotFound_shouldThrowException() {
            when(jobRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> jobService.updateJob(NON_EXISTENT_ID, jobRequest))
                    .isInstanceOf(JobNotFoundException.class)
                    .hasMessage(String.format(JOB_NOT_FOUND_MESSAGE, NON_EXISTENT_ID));

            verify(jobRepository).findById(NON_EXISTENT_ID);
            verify(jobRepository, never()).save(any(Job.class));
            verifyNoMoreInteractions(jobRepository);
        }

        @Test
        @DisplayName("Should update only provided fields when job exists")
        void updateJob_partialUpdate_shouldUpdateOnlyProvidedFields() {
            JobRequestDTO partialUpdate = new JobRequestDTO(null, null, null, "New Location", null);

            when(jobRepository.findById(TEST_ID)).thenReturn(Optional.of(testJob));
            when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

            JobResponseDTO result = jobService.updateJob(TEST_ID, partialUpdate);

            verify(jobRepository).save(any(Job.class));

            assertThat(result.location()).isEqualTo("New Location");
            assertThat(result.title()).isEqualTo(testJob.getTitle());
            assertThat(result.description()).isEqualTo(testJob.getDescription());
            assertThat(result.company()).isEqualTo(testJob.getCompany());
            assertThat(result.salary()).isEqualTo(testJob.getSalary());
        }
    }

    @Nested
    @DisplayName("Delete Job Tests")
    class DeleteJobTest {
        @Test
        @DisplayName("Should deactivate job when job exists")
        void whenJobExists_shouldDeactivateAndSave() {
            when(jobRepository.findById(TEST_ID)).thenReturn(Optional.of(testJob));

            jobService.deleteJob(TEST_ID);
            ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
            verify(jobRepository).save(jobCaptor.capture());
            Job savedJob = jobCaptor.getValue();

            assertThat(savedJob.isActive()).isFalse();
            verify(jobRepository).findById(TEST_ID);
        }

        @Test
        @DisplayName("Should throw JobNotFoundException when job does not exist")
        void whenJobNotFound_shouldThrowException() {
            when(jobRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> jobService.deleteJob(NON_EXISTENT_ID))
                    .isInstanceOf(JobNotFoundException.class);

            verify(jobRepository).findById(NON_EXISTENT_ID);
            verify(jobRepository, never()).save(any(Job.class));
            verifyNoMoreInteractions(jobRepository);
        }
    }

    @Nested
    @DisplayName("Get All Active Jobs Tests")
    class GetAllActiveJobsTests {
        @Test
        @DisplayName("Should return paged model with jobs when active job exist")
        void whenJobExists_shouldReturnPagedJobs() {
            Page<Job> jobPage = new PageImpl<>(List.of(testJob), testPageable, 1);

            when(jobRepository.findByActiveTrue(testPageable)).thenReturn(jobPage);

            PagedModel<EntityModel<JobResponseDTO>> expectedModel = PagedModel.of(
                    List.of(EntityModel.of(expectedResponse)),
                    new PagedModel.PageMetadata(1, 0, 1)
            );
            when(pagedAssembler.toModel(any(Page.class), any(RepresentationModelAssembler.class))).thenReturn(expectedModel);
            PagedModel<EntityModel<JobResponseDTO>> result = jobService.getAllActiveJobs(testPageable);
            JobResponseDTO resultDto = result.getContent().iterator().next().getContent();

            assertThat(resultDto).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(resultDto).extracting(
                    JobResponseDTO::title,
                    JobResponseDTO::company,
                    JobResponseDTO::location,
                    JobResponseDTO::salary,
                    JobResponseDTO::active
            ).containsExactly(
                    "Java Dev",
                    "BigTech",
                    "Remote",
                    5000.0,
                    true
            );

            verify(jobRepository).findByActiveTrue(testPageable);
            verify(pagedAssembler).toModel(any(Page.class), any(RepresentationModelAssembler.class));
        }

        @Test
        @DisplayName("Should return empty paged model when no active jobs exist")
        void whenJobsNotExists_shouldReturnEmptyPage() {
            when(jobRepository.findByActiveTrue(testPageable)).thenReturn(emptyPage);

            PagedModel<EntityModel<JobResponseDTO>> emptyModel = PagedModel.of(
                    List.of(),
                    new PagedModel.PageMetadata(0, 0, 0)
            );
            when(pagedAssembler.toModel(any(Page.class), any(RepresentationModelAssembler.class))).thenReturn(emptyModel);

            PagedModel<EntityModel<JobResponseDTO>> result = jobService.getAllActiveJobs(testPageable);

            assertThat(result.getMetadata()).isNotNull();
            assertThat(result.getContent()).isEmpty();

            verify(jobRepository).findByActiveTrue(testPageable);
            verify(pagedAssembler).toModel(any(Page.class), any(RepresentationModelAssembler.class));
        }
    }

    @Nested
    @DisplayName("Get Job By Recruiter Tests")
    class GetJobByRecruiterTests {
        @Test
        @DisplayName("Should return paged jobs for recruiter when recruiter exist ")
        void whenRecruiterExists_shouldReturnPagedJobs() {
            Page<Job> jobPage = new PageImpl<>(List.of(testJob), testPageable, 1);

            when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
            when(jobRepository.findByPostedBy(testUser, testPageable)).thenReturn(jobPage);
            PagedModel<EntityModel<JobResponseDTO>> expectedModel = PagedModel.of(
                    List.of(EntityModel.of(expectedResponse)),
                    new PagedModel.PageMetadata(1, 0, 1)
            );

            when(pagedAssembler.toModel(any(Page.class), any(RepresentationModelAssembler.class))).thenReturn(expectedModel);
            PagedModel<EntityModel<JobResponseDTO>> result = jobService.getJobsByRecruiter(TEST_USERNAME, testPageable);

            JobResponseDTO resultDto = result.getContent().iterator().next().getContent();

            assertThat(resultDto).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(resultDto.title()).isEqualTo("Java Dev");
            assertThat(resultDto.postedBy()).isEqualTo(TEST_USERNAME);

            verify(userRepository).findByUsername(TEST_USERNAME);
            verify(jobRepository).findByPostedBy(testUser, testPageable);
            verify(pagedAssembler).toModel(any(Page.class), any(RepresentationModelAssembler.class));
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when recruiter does not exists")
        void whenRecruiterNotFound_shouldThrowException() {
            when(userRepository.findByUsername(NON_EXISTENT_USERNAME)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> jobService.getJobsByRecruiter(NON_EXISTENT_USERNAME, testPageable))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessage(USER_NOT_FOUND_MESSAGE + NON_EXISTENT_USERNAME);

            verify(userRepository).findByUsername(NON_EXISTENT_USERNAME);
            verifyNoMoreInteractions(userRepository);
        }

        @Test
        @DisplayName("Should handle empty job list for recruiter")
        void whenNoJobs_shouldReturnEmptyPage() {
            when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
            when(jobRepository.findByPostedBy(testUser, testPageable)).thenReturn(emptyPage);

            PagedModel<EntityModel<JobResponseDTO>> emptyModel = PagedModel.of(
                    List.of(),
                    new PagedModel.PageMetadata(0, 0, 0)
            );
            when(pagedAssembler.toModel(any(Page.class), any(RepresentationModelAssembler.class)))
                    .thenReturn(emptyModel);

            PagedModel<EntityModel<JobResponseDTO>> result = jobService.getJobsByRecruiter(TEST_USERNAME, testPageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getMetadata()).isNotNull();
        }
    }
}
