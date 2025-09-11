package com.github.kzhunmax.jobsearch.service;

import com.github.kzhunmax.jobsearch.dto.request.JobRequestDTO;
import com.github.kzhunmax.jobsearch.dto.response.JobResponseDTO;
import com.github.kzhunmax.jobsearch.exception.JobNotFoundException;
import com.github.kzhunmax.jobsearch.model.Job;
import com.github.kzhunmax.jobsearch.model.User;
import com.github.kzhunmax.jobsearch.repository.JobRepository;
import com.github.kzhunmax.jobsearch.repository.UserRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests for the Job Service")
class JobServiceTest {

    private static final String TEST_USERNAME = "user";
    private static final Long TEST_ID = 1L;
    private static final Long NON_EXISTENT_ID = 99L;
    private static final String NON_EXISTENT_USERNAME = "unknown";
    private static final String USER_NOT_FOUND_MESSAGE = "User not found";
    private static final String JOB_NOT_FOUND_MESSAGE = "Job with id %d not found";

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PagedResourcesAssembler<JobResponseDTO> pagedAssembler;

    @InjectMocks
    private JobService jobService;

    private JobRequestDTO createTestRequest() {
        return new JobRequestDTO(
                "Java Developer",
                "3 years of experience",
                "BigTech",
                "Remote",
                3500.0
        );
    }

    private User createTestUser() {
        User user = new User();
        user.setUsername(TEST_USERNAME);
        user.setId(TEST_ID);
        return user;
    }

    private Job createTestJob(User user) {
        return Job.builder()
                .id(TEST_ID)
                .title("Java Developer")
                .description("3 years of experience")
                .company("BigTech")
                .location("Remote")
                .salary(3500.0)
                .active(true)
                .postedBy(user)
                .build();
    }

    private JobResponseDTO createTestResponseDTO() {
        return new JobResponseDTO(
                TEST_ID,
                "Java Developer",
                "3 years of experience",
                "BigTech",
                "Remote",
                3500.0,
                true,
                TEST_USERNAME
        );
    }

    private Pageable createTestPageable() {
        return PageRequest.of(0, 10);
    }

    @Nested
    @DisplayName("Create job tests")
    class CreateJobTests {
        @Test
        @DisplayName("Should create and return a job when user exists")
        void whenUserExists_shouldCreateAndReturnJob() {
            JobRequestDTO request = createTestRequest();
            User user = createTestUser();
            Job job = createTestJob(user);
            JobResponseDTO expectedResponse = createTestResponseDTO();

            when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));
            when(jobRepository.save(any(Job.class))).thenReturn(job);

            JobResponseDTO result = jobService.createJob(request, TEST_USERNAME);

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
            assertThat(savedJob.getTitle()).isEqualTo(request.title());
            assertThat(savedJob.getDescription()).isEqualTo(request.description());
            assertThat(savedJob.getCompany()).isEqualTo(request.company());
            assertThat(savedJob.getLocation()).isEqualTo(request.location());
            assertThat(savedJob.getSalary()).isEqualTo(request.salary());
            assertThat(savedJob.getPostedBy()).isEqualTo(user);

            verifyNoMoreInteractions(jobRepository, userRepository);
        }

        @Test
        @DisplayName("Should throw UsernameNotFoundException when user does not exist")
        void whenUserNotFound_shouldThrowException() {
            JobRequestDTO request = createTestRequest();

            when(userRepository.findByUsername(NON_EXISTENT_USERNAME)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> jobService.createJob(request, NON_EXISTENT_USERNAME))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessage(USER_NOT_FOUND_MESSAGE);

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
            User user = createTestUser();
            Job job = createTestJob(user);
            JobResponseDTO expectedResponse = createTestResponseDTO();

            when(jobRepository.findById(TEST_ID)).thenReturn(Optional.of(job));

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
            User user = createTestUser();
            Job existingJob = createTestJob(user);
            Job updatedJob = createTestJob(user);
            JobRequestDTO updateRequest = new JobRequestDTO(
                    "Updated title", "Update description", "Updated company",
                    "Updated location", 5000.0
            );

            updatedJob.setTitle("Updated title");
            updatedJob.setDescription("Updated description");
            updatedJob.setCompany("Updated company");
            updatedJob.setLocation("Updated location");
            updatedJob.setSalary(5000.0);

            when(jobRepository.findById(TEST_ID)).thenReturn(Optional.of(existingJob));
            when(jobRepository.save(existingJob)).thenReturn(updatedJob);

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
            JobRequestDTO request = createTestRequest();
            when(jobRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> jobService.updateJob(NON_EXISTENT_ID, request))
                    .isInstanceOf(JobNotFoundException.class)
                    .hasMessage(String.format(JOB_NOT_FOUND_MESSAGE, NON_EXISTENT_ID));

            verify(jobRepository).findById(NON_EXISTENT_ID);
            verify(jobRepository, never()).save(any(Job.class));
            verifyNoMoreInteractions(jobRepository);
        }

        @Test
        @DisplayName("Should update only provided fields when job exists")
        void updateJob_partialUpdate_shouldUpdateOnlyProvidedFields() {
            User user = createTestUser();
            Job existingJob = createTestJob(user);
            JobRequestDTO partialUpdate = new JobRequestDTO(null, null, null, "New Location", null);

            when(jobRepository.findById(TEST_ID)).thenReturn(Optional.of(existingJob));
            when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

            JobResponseDTO result = jobService.updateJob(TEST_ID, partialUpdate);

            verify(jobRepository).save(any(Job.class));

            assertThat(result.location()).isEqualTo("New Location");
            assertThat(result.title()).isEqualTo(existingJob.getTitle());
            assertThat(result.description()).isEqualTo(existingJob.getDescription());
            assertThat(result.company()).isEqualTo(existingJob.getCompany());
            assertThat(result.salary()).isEqualTo(existingJob.getSalary());
        }
    }

    @Nested
    @DisplayName("Delete Job Tests")
    class DeleteJobTest {
        @Test
        @DisplayName("Should deactivate job when job exists")
        void whenJobExists_shouldDeactivateAndSave() {
            User user = createTestUser();
            Job job = createTestJob(user);

            when(jobRepository.findById(TEST_ID)).thenReturn(Optional.of(job));

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
            User user = createTestUser();
            Job job = createTestJob(user);
            Pageable pageable = createTestPageable();
            Page<Job> jobPage = new PageImpl<>(List.of(job), pageable, 1);

            when(jobRepository.findByActiveTrue(pageable)).thenReturn(jobPage);

            JobResponseDTO dto = createTestResponseDTO();
            PagedModel<EntityModel<JobResponseDTO>> expectedModel = PagedModel.of(
                    List.of(EntityModel.of(dto)),
                    new PagedModel.PageMetadata(1, 0, 1)
            );
            when(pagedAssembler.toModel(any(Page.class), any(RepresentationModelAssembler.class))).thenReturn(expectedModel);
            PagedModel<EntityModel<JobResponseDTO>> result = jobService.getAllActiveJobs(pageable);
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
                    "Java Developer",
                    "BigTech",
                    "Remote",
                    3500.0,
                    true
            );

            verify(jobRepository).findByActiveTrue(pageable);
            verify(pagedAssembler).toModel(any(Page.class), any(RepresentationModelAssembler.class));
        }

        @Test
        @DisplayName("Should return empty paged model when no active jobs exist")
        void whenJobsNotExists_shouldReturnEmptyPage() {
            Pageable pageable = createTestPageable();
            Page<Job> emptyPage = Page.empty(pageable);
            when(jobRepository.findByActiveTrue(pageable)).thenReturn(emptyPage);

            PagedModel<EntityModel<JobResponseDTO>> emptyModel = PagedModel.of(
                    List.of(),
                    new PagedModel.PageMetadata(0, 0, 0)
            );
            when(pagedAssembler.toModel(any(Page.class), any(RepresentationModelAssembler.class))).thenReturn(emptyModel);

            PagedModel<EntityModel<JobResponseDTO>> result = jobService.getAllActiveJobs(pageable);

            assertThat(result.getMetadata()).isNotNull();
            assertThat(result.getContent()).isEmpty();

            verify(jobRepository).findByActiveTrue(pageable);
            verify(pagedAssembler).toModel(any(Page.class), any(RepresentationModelAssembler.class));
        }
    }

    @Nested
    @DisplayName("Get Job By Recruiter Tests")
    class GetJobByRecruiterTests {
        @Test
        @DisplayName("Should return paged jobs for recruiter when recruiter exist ")
        void whenRecruiterExists_shouldReturnPagedJobs() {
            User recruiter = createTestUser();
            Job job = createTestJob(recruiter);
            Pageable pageable = createTestPageable();
            Page<Job> jobPage = new PageImpl<>(List.of(job), pageable, 1);

            when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(recruiter));
            when(jobRepository.findByPostedBy(recruiter, pageable)).thenReturn(jobPage);
            JobResponseDTO dto = createTestResponseDTO();
            PagedModel<EntityModel<JobResponseDTO>> expectedModel = PagedModel.of(
                    List.of(EntityModel.of(dto)),
                    new PagedModel.PageMetadata(1, 0, 1)
            );

            when(pagedAssembler.toModel(any(Page.class), any(RepresentationModelAssembler.class))).thenReturn(expectedModel);
            PagedModel<EntityModel<JobResponseDTO>> result = jobService.getJobsByRecruiter(TEST_USERNAME, pageable);

            JobResponseDTO resultDto = result.getContent().iterator().next().getContent();

            assertThat(resultDto).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(resultDto.title()).isEqualTo("Java Developer");
            assertThat(resultDto.postedBy()).isEqualTo(TEST_USERNAME);

            verify(userRepository).findByUsername(TEST_USERNAME);
            verify(jobRepository).findByPostedBy(recruiter, pageable);
            verify(pagedAssembler).toModel(any(Page.class), any(RepresentationModelAssembler.class));
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when recruiter does not exists")
        void whenRecruiterNotFound_shouldThrowException() {
            Pageable pageable = createTestPageable();
            when(userRepository.findByUsername(NON_EXISTENT_USERNAME)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> jobService.getJobsByRecruiter(NON_EXISTENT_USERNAME, pageable))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessage(USER_NOT_FOUND_MESSAGE);

            verify(userRepository).findByUsername(NON_EXISTENT_USERNAME);
            verifyNoMoreInteractions(userRepository);
        }

        @Test
        @DisplayName("Should handle empty job list for recruiter")
        void whenNoJobs_shouldReturnEmptyPage() {
            User recruiter = createTestUser();
            Pageable pageable = createTestPageable();
            Page<Job> emptyPage = Page.empty(pageable);

            when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(recruiter));
            when(jobRepository.findByPostedBy(recruiter, pageable)).thenReturn(emptyPage);

            PagedModel<EntityModel<JobResponseDTO>> emptyModel = PagedModel.of(
                    List.of(),
                    new PagedModel.PageMetadata(0, 0, 0)
            );
            when(pagedAssembler.toModel(any(Page.class), any(RepresentationModelAssembler.class)))
                    .thenReturn(emptyModel);

            PagedModel<EntityModel<JobResponseDTO>> result = jobService.getJobsByRecruiter(TEST_USERNAME, pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getMetadata()).isNotNull();
        }
    }
}
