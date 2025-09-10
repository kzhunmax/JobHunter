package com.github.kzhunmax.jobsearch.service;

import com.github.kzhunmax.jobsearch.dto.request.JobRequestDTO;
import com.github.kzhunmax.jobsearch.dto.response.JobResponseDTO;
import com.github.kzhunmax.jobsearch.exception.JobNotFoundException;
import com.github.kzhunmax.jobsearch.exception.UserNotFoundException;
import com.github.kzhunmax.jobsearch.model.Job;
import com.github.kzhunmax.jobsearch.model.User;
import com.github.kzhunmax.jobsearch.repository.JobRepository;
import com.github.kzhunmax.jobsearch.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
        user.setUsername("user");
        user.setId(1L);
        return user;
    }

    private Job createTestJob(User user) {
        return Job.builder()
                .id(1L)
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
                1L,
                "Java Developer",
                "3 years of experience",
                "BigTech",
                "Remote",
                3500.0,
                true,
                "user"
        );
    }

    @Test
    @DisplayName("Should create and return a job when user exists")
    void createJob_whenUserExists_thenShouldCreateAndReturnJob() {
        JobRequestDTO request = createTestRequest();
        User user = createTestUser();
        Job job = createTestJob(user);
        JobResponseDTO expectedResponse = createTestResponseDTO();

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(jobRepository.save(any(Job.class))).thenReturn(job);

        JobResponseDTO result = jobService.createJob(request, "user");

        assertThat(result).isEqualTo(expectedResponse);
        verify(userRepository).findByUsername("user");
        verify(jobRepository).save(any(Job.class));
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user does not exist")
    void createJob_whenUserNotFound_thenShouldThrowException() {
        JobRequestDTO request = createTestRequest();

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.createJob(request, "unknown"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findByUsername("unknown");
        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    @DisplayName("Should return id of job when job exists")
    void findJobById_whenJobExists_thenShouldReturnJob() {
        User user = createTestUser();
        Job job = createTestJob(user);
        JobResponseDTO expectedResponse = createTestResponseDTO();

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        JobResponseDTO result = jobService.getJobById(1L);

        assertThat(result).isEqualTo(expectedResponse);
        verify(jobRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw JobNotFoundException when job does not exist")
    void findJobById_whenJobNotFound_thenShouldThrowException() {
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.getJobById(99L))
                .isInstanceOf(JobNotFoundException.class);

        verify(jobRepository).findById(99L);
    }

    @Test
    @DisplayName("Should update and return new job details when job exists")
    void updateJob_whenJobExists_thenShouldReturnUpdatedJobDetails() {
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

        when(jobRepository.findById(1L)).thenReturn(Optional.of(existingJob));
        when(jobRepository.save(existingJob)).thenReturn(updatedJob);

        JobResponseDTO result = jobService.updateJob(1L, updateRequest);

        assertThat(result.title()).isEqualTo("Updated title");
        assertThat(result.description()).isEqualTo("Updated description");
        assertThat(result.salary()).isEqualTo(5000.0);

        verify(jobRepository).findById(1L);
        verify(jobRepository).save(existingJob);
    }

    @Test
    @DisplayName("Should throw JobNotFoundException when job does not exist")
    void updateJob_whenJobNotFound_thenShouldThrowException() {
        JobRequestDTO request = createTestRequest();
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.updateJob(99L, request))
                .isInstanceOf(JobNotFoundException.class);

        verify(jobRepository).findById(99L);
        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    @DisplayName("Should deactivate job when job exists")
    void deleteJob_whenJobExists_thenShouldDeactivateAndSave() {
        User user = createTestUser();
        Job job = createTestJob(user);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobRepository.save(job)).thenReturn(job);

        jobService.deleteJob(1L);

        assertThat(job.isActive()).isEqualTo(false);
        verify(jobRepository).findById(1L);
        verify(jobRepository).save(job);
    }

    @Test
    @DisplayName("Should throw JobNotFoundException when job does not exist")
    void deleteJob_whenJobNotFound_thenShouldThrowException() {
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.deleteJob(99L))
                .isInstanceOf(JobNotFoundException.class);

        verify(jobRepository).findById(99L);
        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    @DisplayName("Should return paged model with jobs when active job exist")
    void getAllActiveJobs_whenJobExists_shouldReturnPagedJobs() {
        User user = createTestUser();
        Job job = createTestJob(user);
        Pageable pageable = PageRequest.of(0, 10);
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
        assertThat(resultDto.title()).isEqualTo("Java Developer");
        assertThat(resultDto.location()).isEqualTo("Remote");

        verify(jobRepository).findByActiveTrue(pageable);
        verify(pagedAssembler).toModel(any(Page.class), any(RepresentationModelAssembler.class));
        verifyNoMoreInteractions(jobRepository, pagedAssembler);
    }

    @Test
    @DisplayName("Should return empty paged model when no active jobs exist")
    void getAllActiveJobs_whenJobsNotExists_shouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Job> emptyPage = Page.empty(pageable);
        when(jobRepository.findByActiveTrue(pageable)).thenReturn(emptyPage);

        PagedModel<EntityModel<JobResponseDTO>> emptyModel = PagedModel.of(
                List.of(), new PagedModel.PageMetadata(0, 0, 0));
        when(pagedAssembler.toModel(any(Page.class), any(RepresentationModelAssembler.class))).thenReturn(emptyModel);

        PagedModel<EntityModel<JobResponseDTO>> result = jobService.getAllActiveJobs(pageable);

        assertThat(result.getMetadata()).isNotNull();
        assertThat(result.getContent()).isEmpty();

        verify(jobRepository).findByActiveTrue(pageable);
        verify(pagedAssembler).toModel(any(Page.class), any(RepresentationModelAssembler.class));
    }

    @Test
    @DisplayName("Should return paged jobs for recruiter when recruiter exist ")
    void getJobsByRecruiter_whenRecruiterExists_shouldReturnPagedJobs() {
        User recruiter = createTestUser();
        Job job = createTestJob(recruiter);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Job> jobPage = new PageImpl<>(List.of(job), pageable, 1);

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(recruiter));
        when(jobRepository.findByPostedBy(recruiter, pageable)).thenReturn(jobPage);
        JobResponseDTO dto = createTestResponseDTO();
        PagedModel<EntityModel<JobResponseDTO>> expectedModel = PagedModel.of(
                List.of(EntityModel.of(dto)),
                new PagedModel.PageMetadata(1, 0, 1)
        );

        when(pagedAssembler.toModel(any(Page.class), any(RepresentationModelAssembler.class))).thenReturn(expectedModel);
        PagedModel<EntityModel<JobResponseDTO>> result = jobService.getJobsByRecruiter("user", pageable);

        JobResponseDTO resultDto = result.getContent().iterator().next().getContent();

        assertThat(resultDto).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(resultDto.title()).isEqualTo("Java Developer");
        assertThat(resultDto.postedBy()).isEqualTo("user");

        verify(userRepository).findByUsername("user");
        verify(jobRepository).findByPostedBy(recruiter, pageable);
        verify(pagedAssembler).toModel(any(Page.class), any(RepresentationModelAssembler.class));
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when recruiter does not exists")
    void getJobsByRecruiter_whenRecruiterNotFound_shouldThrowException() {
        Pageable pageable = PageRequest.of(0, 1);
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.getJobsByRecruiter("unknown", pageable))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findByUsername("unknown");
        verifyNoInteractions(jobRepository);
        verifyNoInteractions(pagedAssembler);
    }
}
