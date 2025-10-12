package com.github.kzhunmax.jobsearch.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kzhunmax.jobsearch.dto.request.JobRequestDTO;
import com.github.kzhunmax.jobsearch.dto.response.JobResponseDTO;
import com.github.kzhunmax.jobsearch.exception.JobNotFoundException;
import com.github.kzhunmax.jobsearch.security.JobSecurityService;
import com.github.kzhunmax.jobsearch.security.JwtService;
import com.github.kzhunmax.jobsearch.security.UserDetailsServiceImpl;
import com.github.kzhunmax.jobsearch.service.JobService;
import com.github.kzhunmax.jobsearch.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.github.kzhunmax.jobsearch.util.TestDataFactory.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobController.class)
@EnableMethodSecurity
@DisplayName("JobController Tests")
class JobControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobService jobService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private JwtService jwtService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public JobSecurityService jobSecurityService() {
            return Mockito.mock(JobSecurityService.class);
        }
    }

    @Autowired
    private JobSecurityService jobSecurityService;

    @Autowired
    private ObjectMapper objectMapper;

    private JobRequestDTO validJobRequest;
    private JobResponseDTO jobResponse;
    private JobRequestDTO invalidJobRequest;

    @BeforeEach
    void setUp() {
        validJobRequest = createJobRequest();
        jobResponse = createJobResponse(TEST_ID);
        invalidJobRequest = createInvalidJobRequest();
    }

    @Nested
    @DisplayName("Create Job Endpoint Tests")
    class CreateJob {
        @Test
        @DisplayName("Returns created job when request is valid and user has recruiter role")
        @WithMockUser(username = TEST_USERNAME, roles = "RECRUITER")
        void withValidRequestAndRecruiterRole_returnCreatedJob() throws Exception {
            when(jobService.createJob(any(JobRequestDTO.class), eq(TEST_USERNAME))).thenReturn(jobResponse);

            mockMvc.perform(post("/api/jobs")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validJobRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.id").value(1L))
                    .andExpect(jsonPath("$.data.title").value("Java Dev"))
                    .andExpect(jsonPath("$.data.company").value("BigTech"))
                    .andExpect(jsonPath("$.data.location").value("Remote"))
                    .andExpect(jsonPath("$.data.salary").value(5000.0))
                    .andExpect(jsonPath("$.errors").isEmpty());
        }

        @Test
        @DisplayName("Returns 403 Forbidden when user does not have recruiter role")
        @WithMockUser(username = TEST_USERNAME, roles = "USER")
        void withUnauthorizedUserRole_returnsForbidden() throws Exception {
            mockMvc.perform(post("/api/jobs")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validJobRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Returns 400 Bad Request when request validation fails")
        @WithMockUser(username = TEST_USERNAME, roles = "RECRUITER")
        void withInvalidRequest_returnsBadRequest() throws Exception {
            when(jobService.createJob(any(JobRequestDTO.class), eq(TEST_USERNAME))).thenReturn(jobResponse);

            mockMvc.perform(post("/api/jobs")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidJobRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @WithMockUser(username = TEST_USERNAME, roles = "USER")
    @DisplayName("List Jobs Endpoint Tests")
    class ListJobs {
        @Test
        @DisplayName("Returns paginated list of active jobs")
        void withActiveJobs_returnsPagedJobs() throws Exception {
            List<JobResponseDTO> jobList = List.of(
                    createJobResponse(1L),
                    createJobResponse(2L)
            );

            PagedModel<EntityModel<JobResponseDTO>> pagedJobs = PagedModel.of(
                    jobList.stream().map(EntityModel::of).toList(),
                    new PagedModel.PageMetadata(20, 0, 2)
            );

            when(jobService.getAllActiveJobs(any(Pageable.class))).thenReturn(pagedJobs);

            mockMvc.perform(get("/api/jobs")
                            .param("page", "0")
                            .param("size", "20")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.page.size").value(20))
                    .andExpect(jsonPath("$.data.page.totalPages").value(1))
                    .andExpect(jsonPath("$.data.page.totalElements").value(2))
                    .andExpect(jsonPath("$.errors").isEmpty());
        }

        @Test
        @DisplayName("Returns empty list when no active jobs")
        void withNoActiveJobs_returnsEmptyList() throws Exception {
            PagedModel<EntityModel<JobResponseDTO>> emptyPagedJobs = PagedModel.of(
                    List.of(),
                    new PagedModel.PageMetadata(20, 0, 0)
            );
            when(jobService.getAllActiveJobs(any(Pageable.class))).thenReturn(emptyPagedJobs);

            mockMvc.perform(get("/api/jobs")
                            .param("page", "0")
                            .param("size", "20")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isEmpty())
                    .andExpect(jsonPath("$.data.page.totalElements").value(0))
                    .andExpect(jsonPath("$.errors").isEmpty());
        }

        @Test
        @DisplayName("Support pagination parameters")
        void withPagination_returnsCorrectPage() throws Exception {
            List<JobResponseDTO> jobList = List.of(
                    createJobResponse(3L)
            );

            PagedModel<EntityModel<JobResponseDTO>> pagedJobs = PagedModel.of(
                    jobList.stream().map(EntityModel::of).toList(),
                    new PagedModel.PageMetadata(1, 1, 3)
            );

            when(jobService.getAllActiveJobs(any(Pageable.class))).thenReturn(pagedJobs);

            mockMvc.perform(get("/api/jobs")
                            .param("page", "1")
                            .param("size", "1")
                            .param("sort", "createdAt,desc")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content[0].id").value(3))
                    .andExpect(jsonPath("$.data.page.size").value(1))
                    .andExpect(jsonPath("$.data.page.number").value(1))
                    .andExpect(jsonPath("$.data.page.totalElements").value(3))
                    .andExpect(jsonPath("$.errors").isEmpty());
        }

        @Test
        @WithMockUser(username = TEST_USERNAME, roles = "USER")
        @DisplayName("Returns paginated list of active jobs for authenticated user")
        void withActiveJobsAndAuthenticated_returnsPagedJobs() throws Exception {
            List<JobResponseDTO> jobList = List.of(
                    createJobResponse(1L),
                    createJobResponse(2L)
            );

            PagedModel<EntityModel<JobResponseDTO>> pagedJobs = PagedModel.of(
                    jobList.stream().map(EntityModel::of).toList(),
                    new PagedModel.PageMetadata(20, 0, 2)
            );

            when(jobService.getAllActiveJobs(any(Pageable.class))).thenReturn(pagedJobs);

            mockMvc.perform(get("/api/jobs")
                            .param("page", "0")
                            .param("size", "20")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.page.totalElements").value(2))
                    .andExpect(jsonPath("$.errors").isEmpty());
        }
    }

    @Nested
    @WithMockUser(username = TEST_USERNAME, roles = "USER")
    @DisplayName("Get Job By ID Endpoint Tests")
    class GetJobById {
        @Test
        @DisplayName("Returns job by ID when exists")
        void withExistingJob_returnsJob() throws Exception {
            when(jobService.getJobById(TEST_ID)).thenReturn(jobResponse);

            mockMvc.perform(get("/api/jobs/{jobId}", TEST_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.title").value("Java Dev"))
                    .andExpect(jsonPath("$.data.company").value("BigTech"))
                    .andExpect(jsonPath("$.data.location").value("Remote"))
                    .andExpect(jsonPath("$.data.salary").value(5000.0))
                    .andExpect(jsonPath("$.errors").isEmpty());
        }

        @Test
        @DisplayName("Returns 404 Not Found when job does not exist")
        void withNonExistingJob_returnsNotFound() throws Exception {
            when(jobService.getJobById(NON_EXISTENT_ID)).thenThrow(new JobNotFoundException(NON_EXISTENT_ID));

            mockMvc.perform(get("/api/jobs/{jobId}", NON_EXISTENT_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors").isNotEmpty())
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("Returns 400 Bad Request when invalid non-numeric ID")
        void withNonNumericId_returnsBadRequest() throws Exception {
            String nonNumericId = "abc123";

            mockMvc.perform(get("/api/jobs/{jobId}", nonNumericId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors").isNotEmpty())
                    .andExpect(jsonPath("$.errors[0].code").value("TYPE_MISMATCH"))
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @WithMockUser(username = TEST_USERNAME, roles = "RECRUITER")
    @DisplayName("Update Job Endpoint Tests")
    class UpdateJob {
        @Test
        @DisplayName("Returns updated job when job owner updates")
        void withJobOwner_returnsUpdatedJob() throws Exception {
            JobRequestDTO updatedRequest = TestDataFactory.updateJobRequest();
            JobResponseDTO updatedResponse = TestDataFactory.updateJobResponse(TEST_ID);

            when(jobSecurityService.isJobOwner(eq(TEST_ID), any(Authentication.class))).thenReturn(true);
            when(jobService.updateJob(eq(TEST_ID), any(JobRequestDTO.class))).thenReturn(updatedResponse);

            mockMvc.perform(put("/api/jobs/{jobId}", TEST_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.title").value("Updated title"))
                    .andExpect(jsonPath("$.data.company").value("Updated company"))
                    .andExpect(jsonPath("$.data.location").value("Updated location"))
                    .andExpect(jsonPath("$.data.salary").value(5000.0))
                    .andExpect(jsonPath("$.errors").isEmpty());
        }

        @Test
        @DisplayName("Returns 403 Forbidden when non-owner tries to update job")
        void withUnauthorizedNonOwner_returnsForbidden() throws Exception {
            JobRequestDTO updatedRequest = updateJobRequest();

            when(jobSecurityService.isJobOwner(eq(TEST_ID), any(Authentication.class))).thenReturn(false);

            mockMvc.perform(put("/api/jobs/{jobId}", TEST_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Returns 400 Bad Request when request validation fails")
        void withInvalidRequest_returnsBadRequest() throws Exception {

            when(jobSecurityService.isJobOwner(eq(TEST_ID), any(Authentication.class))).thenReturn(true);
            when(jobService.updateJob(eq(TEST_ID), any(JobRequestDTO.class))).thenReturn(jobResponse);

            mockMvc.perform(put("/api/jobs/{jobId}", TEST_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidJobRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors").isNotEmpty())
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("Returns 400 Bad Request when invalid non-numeric ID")
        void withNonNumericId_returnsBadRequest() throws Exception {
            String nonNumericId = "abc123";

            mockMvc.perform(put("/api/jobs/{jobId}", nonNumericId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors").isNotEmpty())
                    .andExpect(jsonPath("$.errors[0].code").value("TYPE_MISMATCH"))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("Returns 404 Not Found when job does not exist")
        void withNonExistingJob_returnsNotFound() throws Exception {
            JobRequestDTO updatedJobRequest = TestDataFactory.updateJobRequest();

            when(jobSecurityService.isJobOwner(eq(NON_EXISTENT_ID), any(Authentication.class))).thenReturn(true);
            when(jobService.updateJob(eq(NON_EXISTENT_ID), any(JobRequestDTO.class))).thenThrow(new JobNotFoundException(NON_EXISTENT_ID));

            mockMvc.perform(put("/api/jobs/{jobId}", NON_EXISTENT_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedJobRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors").isNotEmpty())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @WithMockUser(username = TEST_USERNAME, roles = "RECRUITER")
    @DisplayName("Delete Job Endpoint Tests")
    class DeleteJob {
        @Test
        @DisplayName("Returns 204 No Content when job owner deletes")
        void withJobOwner_returnsNoContent() throws Exception {
            when(jobSecurityService.isJobOwner(eq(TEST_ID), any(Authentication.class))).thenReturn(true);

            mockMvc.perform(delete("/api/jobs/{jobId}", TEST_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Returns 403 Forbidden when non-owner tries to delete job")
        void withUnauthorizedNonOwner_returnsForbidden() throws Exception {
            when(jobSecurityService.isJobOwner(eq(TEST_ID), any(Authentication.class))).thenReturn(false);

            mockMvc.perform(delete("/api/jobs/{jobId}", TEST_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Returns 400 Bad Request when invalid non-numeric ID")
        void withNonNumericId_returnsBadRequest() throws Exception {
            String nonNumericId = "abc123";

            mockMvc.perform(delete("/api/jobs/{jobId}", nonNumericId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors").isNotEmpty())
                    .andExpect(jsonPath("$.errors[0].code").value("TYPE_MISMATCH"))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("Returns 404 Not Found when job does not exist")
        void withNonExistingJob_returnsNotFound() throws Exception {
            when(jobSecurityService.isJobOwner(eq(NON_EXISTENT_ID), any(Authentication.class))).thenReturn(true);
            doThrow(new JobNotFoundException(NON_EXISTENT_ID)).when(jobService).deleteJob(NON_EXISTENT_ID);

            mockMvc.perform(delete("/api/jobs/{jobId}", NON_EXISTENT_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors").isNotEmpty())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("Get My Jobs Endpoint Tests")
    class GetMyJobs {
        @Test
        @WithMockUser(username = TEST_USERNAME, roles = "RECRUITER")
        @DisplayName("Returns paginated list of active jobs")
        void withActiveJobs_returnsPagedJobs() throws Exception {
            List<JobResponseDTO> jobList = List.of(
                    createJobResponse(1L),
                    createJobResponse(2L)
            );

            PagedModel<EntityModel<JobResponseDTO>> pagedJobs = PagedModel.of(
                    jobList.stream().map(EntityModel::of).toList(),
                    new PagedModel.PageMetadata(20, 0, 2)
            );

            when(jobService.getJobsByRecruiter(eq(TEST_USERNAME), any(Pageable.class))).thenReturn(pagedJobs);

            mockMvc.perform(get("/api/jobs/my-jobs")
                            .param("page", "0")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.page.size").value(20))
                    .andExpect(jsonPath("$.data.page.totalElements").value(2))
                    .andExpect(jsonPath("$.data.page.totalPages").value(1))
                    .andExpect(jsonPath("$.errors").isEmpty());
        }

        @Test
        @WithMockUser(username = TEST_USERNAME, roles = "RECRUITER")
        @DisplayName("Returns empty list when no active jobs")
        void withNoActiveJobs_returnsEmptyList() throws Exception {
            PagedModel<EntityModel<JobResponseDTO>> emptyPagedJobs = PagedModel.of(
                    List.of(),
                    new PagedModel.PageMetadata(20, 0, 0)
            );
            when(jobService.getJobsByRecruiter(eq(TEST_USERNAME), any(Pageable.class))).thenReturn(emptyPagedJobs);

            mockMvc.perform(get("/api/jobs/my-jobs")
                            .param("page", "0")
                            .param("size", "20")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isEmpty())
                    .andExpect(jsonPath("$.data.page.totalElements").value(0))
                    .andExpect(jsonPath("$.errors").isEmpty());
        }

        @Test
        @WithMockUser(username = TEST_USERNAME, roles = "USER")
        @DisplayName("Returns 403 Forbidden when non-recruiter tries to find his jobs")
        void withUnauthorizedNonRecruiter_returnsForbidden() throws Exception {
            mockMvc.perform(get("/api/jobs/my-jobs")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }
}
