package com.github.kzhunmax.jobsearch.job.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kzhunmax.jobsearch.exception.JobNotFoundException;
import com.github.kzhunmax.jobsearch.job.dto.JobRequestDTO;
import com.github.kzhunmax.jobsearch.job.dto.JobResponseDTO;
import com.github.kzhunmax.jobsearch.job.model.es.JobDocument;
import com.github.kzhunmax.jobsearch.job.service.JobService;
import com.github.kzhunmax.jobsearch.job.service.search.JobSearchService;
import com.github.kzhunmax.jobsearch.security.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.github.kzhunmax.jobsearch.util.TestDataFactory.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobController.class)
@AutoConfigureMockMvc(addFilters = false)
@EnableMethodSecurity
@DisplayName("JobController Tests")
class JobControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobService jobService;

    @MockitoBean
    private JobSearchService jobSearchService;

    @MockitoBean
    private RateLimitingService rateLimitingService;

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
        validJobRequest = createJobRequest(TEST_ID);
        jobResponse = createJobResponse(TEST_ID, TEST_COMPANY_NAME, TEST_EMAIL);
        invalidJobRequest = createInvalidJobRequest();
    }

    @Nested
    @DisplayName("Create Job Endpoint Tests")
    class CreateJob {
        @Test
        @DisplayName("Returns created job when request is valid and user has recruiter role")
        @WithUserDetails(TEST_EMAIL)
        void withValidRequestAndRecruiterRole_returnCreatedJob() throws Exception {
            when(jobService.createJob(any(JobRequestDTO.class), eq(TEST_ID))).thenReturn(jobResponse);

            mockMvc.perform(post("/api/jobs")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validJobRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.id").value(TEST_ID))
                    .andExpect(jsonPath("$.data.title").value(TEST_TITLE))
                    .andExpect(jsonPath("$.data.company").value(TEST_COMPANY_NAME))
                    .andExpect(jsonPath("$.data.postedBy").value(TEST_EMAIL))
                    .andExpect(jsonPath("$.errors").isEmpty());
        }

        @Test
        @DisplayName("Returns 403 Forbidden when user does not have recruiter role")
        @WithMockUser(roles = "CANDIDATE")
        void withUnauthorizedUserRole_returnsForbidden() throws Exception {
            mockMvc.perform(post("/api/jobs")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validJobRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Returns 400 Bad Request when request validation fails")
        @WithMockUser(roles = "RECRUITER")
        void withInvalidRequest_returnsBadRequest() throws Exception {
            when(jobService.createJob(any(JobRequestDTO.class), eq(TEST_ID))).thenReturn(jobResponse);

            mockMvc.perform(post("/api/jobs")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidJobRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors").isNotEmpty());
        }
    }

    @Nested
    @WithMockUser
    @DisplayName("List Jobs Endpoint Tests")
    class ListJobs {
        @Test
        @DisplayName("Returns paginated list of active jobs")
        void withActiveJobs_returnsPagedJobs() throws Exception {
            PagedModel<EntityModel<JobResponseDTO>> pagedJobs = PagedModel.of(
                    List.of(EntityModel.of(jobResponse)),
                    new PagedModel.PageMetadata(20, 0, 1)
            );

            when(jobService.getAllActiveJobs(any(Pageable.class), any())).thenReturn(pagedJobs);

            mockMvc.perform(get("/api/jobs")
                            .param("page", "0")
                            .param("size", "20")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.page.size").value(20))
                    .andExpect(jsonPath("$.data.page.totalElements").value(1))
                    .andExpect(jsonPath("$.errors").isEmpty());
        }
    }

    @Nested
    @WithMockUser
    @DisplayName("Get Job By ID Endpoint Tests")
    class GetJobById {
        @Test
        @DisplayName("Returns job by ID when exists")
        void withExistingJob_returnsJob() throws Exception {
            when(jobService.getJobById(TEST_ID)).thenReturn(jobResponse);

            mockMvc.perform(get("/api/jobs/{jobId}", TEST_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(TEST_ID))
                    .andExpect(jsonPath("$.data.title").value(TEST_TITLE))
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
                    .andExpect(jsonPath("$.errors[0].code").value("TYPE_MISMATCH"));
        }
    }

    @Nested
    @DisplayName("Update Job Endpoint Tests")
    class UpdateJob {
        @Test
        @DisplayName("Returns updated job when job owner updates")
        @WithMockUser(roles = "RECRUITER")
        void withJobOwner_returnsUpdatedJob() throws Exception {
            JobRequestDTO updatedRequest = updateJobRequest(TEST_ID);
            JobResponseDTO updatedResponse = updateJobResponse(TEST_ID, "BigTech", TEST_EMAIL);

            when(jobSecurityService.isJobOwner(eq(TEST_ID), any(Authentication.class))).thenReturn(true);
            when(jobService.updateJob(eq(TEST_ID), any(JobRequestDTO.class))).thenReturn(updatedResponse);

            mockMvc.perform(put("/api/jobs/{jobId}", TEST_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(TEST_ID))
                    .andExpect(jsonPath("$.data.title").value("Updated title"))
                    .andExpect(jsonPath("$.errors").isEmpty());
        }

        @Test
        @WithMockUser(roles = "CANDIDATE")
        @DisplayName("Returns 403 Forbidden when non-owner tries to update job")
        void withUnauthorizedNonOwner_returnsForbidden() throws Exception {
            JobRequestDTO updatedRequest = updateJobRequest(TEST_ID);

            when(jobSecurityService.isJobOwner(eq(TEST_ID), any(Authentication.class))).thenReturn(false);

            mockMvc.perform(put("/api/jobs/{jobId}", TEST_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "RECRUITER")
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
        @WithMockUser(roles = "RECRUITER")
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
        @WithMockUser(roles = "RECRUITER")
        @DisplayName("Returns 404 Not Found when job does not exist")
        void withNonExistingJob_returnsNotFound() throws Exception {
            JobRequestDTO updatedJobRequest = updateJobRequest(TEST_ID);

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
    @DisplayName("Delete Job Endpoint Tests")
    class DeleteJob {
        @Test
        @DisplayName("Returns 204 No Content when job owner deletes")
        @WithMockUser(roles = "RECRUITER")
        void withJobOwner_returnsNoContent() throws Exception {
            when(jobSecurityService.isJobOwner(eq(TEST_ID), any(Authentication.class))).thenReturn(true);

            mockMvc.perform(delete("/api/jobs/{jobId}", TEST_ID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(roles = "CANDIDATE")
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
                    .andExpect(jsonPath("$.errors[0].code").value("TYPE_MISMATCH"))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("Returns 404 Not Found when job does not exist")
        @WithUserDetails(TEST_EMAIL)
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
        @WithUserDetails(TEST_EMAIL)
        @DisplayName("Returns paginated list of active jobs")
        void withActiveJobs_returnsPagedJobs() throws Exception {

            PagedModel<EntityModel<JobResponseDTO>> pagedJobs = PagedModel.of(
                    List.of(EntityModel.of(jobResponse)),
                    new PagedModel.PageMetadata(20, 0, 1)
            );

            when(jobService.getJobsByRecruiter(eq(TEST_ID), any(Pageable.class), any())).thenReturn(pagedJobs);

            mockMvc.perform(get("/api/jobs/my-jobs")
                            .param("page", "0")
                            .param("size", "20")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.page.size").value(20))
                    .andExpect(jsonPath("$.data.page.totalElements").value(1))
                    .andExpect(jsonPath("$.errors").isEmpty());
        }

        @Test
        @WithMockUser(roles = "CANDIDATE")
        @DisplayName("Returns 403 Forbidden when non-recruiter tries to find his jobs")
        void withUnauthorizedNonRecruiter_returnsForbidden() throws Exception {
            mockMvc.perform(get("/api/jobs/my-jobs")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Search Jobs Endpoint Tests")
    class SearchJobs {
        @Test
        @DisplayName("Returns search results for authenticated user")
        @WithUserDetails(TEST_EMAIL)
        void searchJobs_withValidUser_returnsResults() throws Exception {
            JobDocument doc = JobDocument.builder().id(TEST_ID).title("Java Developer").build();
            PagedModel<EntityModel<JobDocument>> pagedDocs = PagedModel.of(
                    List.of(EntityModel.of(doc)),
                    new PagedModel.PageMetadata(20, 0, 1)
            );

            doNothing().when(rateLimitingService).consumeToken(anyString(), any(PricingPlan.class), anyString());
            when(jobSearchService.searchJobs(eq("Java"), eq("Remote"), eq(null), any(Pageable.class), any()))
                    .thenReturn(pagedDocs);

            mockMvc.perform(get("/api/jobs/search")
                            .param("query", "Java")
                            .param("location", "Remote"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].id").value(TEST_ID))
                    .andExpect(jsonPath("$.data.content[0].title").value("Java Developer"))
                    .andExpect(jsonPath("$.errors").isEmpty());
        }

        @Test
        @DisplayName("Returns 429 Too Many Requests when rate limit exceeded")
        @WithUserDetails(TEST_EMAIL)
        void searchJobs_whenRateLimitExceeded_returnsTooManyRequests() throws Exception {
            doThrow(new com.github.kzhunmax.jobsearch.exception.RateLimitExceededException())
                    .when(rateLimitingService).consumeToken(anyString(), any(PricingPlan.class), anyString());

            mockMvc.perform(get("/api/jobs/search")
                            .param("query", "Java"))
                    .andExpect(status().isTooManyRequests())
                    .andExpect(jsonPath("$.errors[0].code").value("RATE_LIMIT_EXCEEDED"));
        }
    }
}
