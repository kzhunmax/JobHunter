package com.github.kzhunmax.jobsearch.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kzhunmax.jobsearch.dto.request.JobApplicationRequestDTO;
import com.github.kzhunmax.jobsearch.dto.response.JobApplicationResponseDTO;
import com.github.kzhunmax.jobsearch.exception.ApiException;
import com.github.kzhunmax.jobsearch.model.ApplicationStatus;
import com.github.kzhunmax.jobsearch.model.Job;
import com.github.kzhunmax.jobsearch.model.JobApplication;
import com.github.kzhunmax.jobsearch.model.User;
import com.github.kzhunmax.jobsearch.security.JobSecurityService;
import com.github.kzhunmax.jobsearch.security.JwtService;
import com.github.kzhunmax.jobsearch.security.UserDetailsServiceImpl;
import com.github.kzhunmax.jobsearch.service.JobApplicationService;
import com.github.kzhunmax.jobsearch.service.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.github.kzhunmax.jobsearch.util.TestDataFactory.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobApplicationController.class)
@EnableMethodSecurity
@DisplayName("JobApplicationController Tests")
class JobApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobApplicationService jobApplicationService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private JobService jobService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public JobSecurityService jobSecurityService() {
            return Mockito.mock(JobSecurityService.class);
        }
    }

    @Autowired
    private JobSecurityService jobSecurityService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Job testJob;
    private JobApplication jobApplication;
    private JobApplicationResponseDTO jobApplicationResponseDTO;
    private JobApplicationRequestDTO jobApplicationRequestDTO;
    private PagedModel<EntityModel<JobApplicationResponseDTO>> pagedModel;
    private Pageable pageable;


    @BeforeEach
    void setUp() {
        testUser = createUser(TEST_ID, TEST_USERNAME);
        testJob = createJob(TEST_ID, testUser, true);
        jobApplication = createJobApplication(TEST_ID, testUser, testJob);
        jobApplicationResponseDTO = createJobApplicationResponseDTO(jobApplication);
        jobApplicationRequestDTO = createJobApplicationRequestDTO("Cover Letter");
        pageable = PageRequest.of(0, 20);
        List<EntityModel<JobApplicationResponseDTO>> content = List.of(EntityModel.of(jobApplicationResponseDTO));
        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(20, 0, 1, content.size());
        pagedModel = PagedModel.of(content, metadata);
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, roles = "CANDIDATE")
    @DisplayName("Should apply for job as CANDIDATE")
    void applyForJob_asUser_returnsResponse() throws Exception {
        when(jobApplicationService.applyToJob(eq(TEST_ID), eq(TEST_USERNAME), eq("Cover Letter")))
                .thenReturn(jobApplicationResponseDTO);

        mockMvc.perform(post("/api/applications/apply/{jobId}", TEST_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jobApplicationRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(TEST_ID))
                .andExpect(jsonPath("$.data.jobId").value(TEST_ID))
                .andExpect(jsonPath("$.data.candidateUsername").value(TEST_USERNAME))
                .andExpect(jsonPath("$.data.status").value("APPLIED"))
                .andExpect(jsonPath("$.data.status").value("APPLIED"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, roles = "CANDIDATE")
    @DisplayName("Should return 404 for job not found")
    void applyForJob_jobNotFound_shouldReturnNotFound() throws Exception {
        when(jobApplicationService.applyToJob(eq(NON_EXISTENT_ID), eq(TEST_USERNAME), eq("Cover Letter")))
                .thenThrow(new ApiException(JOB_NOT_FOUND_MESSAGE.formatted(NON_EXISTENT_ID), HttpStatus.NOT_FOUND, "JOB_NOT_FOUND"));

        mockMvc.perform(post("/api/applications/apply/{jobId}", NON_EXISTENT_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jobApplicationRequestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].code").value("JOB_NOT_FOUND"))
                .andExpect(jsonPath("$.errors[0].message").value(JOB_NOT_FOUND_MESSAGE.formatted(NON_EXISTENT_ID)))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, roles = "CANDIDATE")
    @DisplayName("Should return 409 for duplicate application")
    void applyForJob_duplicateApplication_shouldReturnConflict() throws Exception {
        when(jobApplicationService.applyToJob(eq(TEST_ID), eq(TEST_USERNAME), eq("Cover Letter")))
                .thenThrow(new ApiException("User has already applied to this job", HttpStatus.CONFLICT, "DUPLICATE_APPLICATION"));

        mockMvc.perform(post("/api/applications/apply/{jobId}", TEST_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jobApplicationRequestDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors[0].code").value("DUPLICATE_APPLICATION"))
                .andExpect(jsonPath("$.errors[0].message").value("User has already applied to this job"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should get applications for job as ADMIN")
    void getApplicationsForJob_asAdmin_returnsApplications() throws Exception {
        when(jobApplicationService.getApplicationsForJob(eq(TEST_ID), any(Pageable.class)))
                .thenReturn(pagedModel);

        mockMvc.perform(get("/api/applications/job/{jobId}", TEST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(TEST_ID))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, roles = "CANDIDATE")
    @DisplayName("Should return 403 for unauthorized access to job applications")
    void getApplicationsForJob_unauthorized_shouldReturnForbidden() throws Exception {
        when(jobSecurityService.isJobOwner(eq(TEST_ID), any(Authentication.class))).thenReturn(false);

        mockMvc.perform(get("/api/applications/job/{jobId}", TEST_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 for job not found in applications")
    void getApplicationsForJob_jobNotFound_shouldReturnNotFound() throws Exception {
        when(jobApplicationService.getApplicationsForJob(eq(NON_EXISTENT_ID), any(Pageable.class)))
                .thenThrow(new ApiException(JOB_NOT_FOUND_MESSAGE.formatted(NON_EXISTENT_ID), HttpStatus.NOT_FOUND, "JOB_NOT_FOUND"));

        mockMvc.perform(get("/api/applications/job/{jobId}", NON_EXISTENT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].code").value("JOB_NOT_FOUND"))
                .andExpect(jsonPath("$.errors[0].message").value(JOB_NOT_FOUND_MESSAGE.formatted(NON_EXISTENT_ID)))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @WithMockUser
    @DisplayName("Returns 400 Bad Request when invalid non-numeric ID")
    void getApplicationsForJob_withNonNumericId_returnsBadRequest() throws Exception {
        String nonNumericId = "abc123";

        mockMvc.perform(get("/api/applications/job/{jobId}", nonNumericId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].code").value("TYPE_MISMATCH"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    @DisplayName("Should get my applications as CANDIDATE")
    void getMyApplications_asUser_returnsApplications() throws Exception {
        when(jobApplicationService.getApplicationsByCandidate(eq(TEST_USERNAME), any(Pageable.class)))
                .thenReturn(pagedModel);

        mockMvc.perform(get("/api/applications/my-applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(TEST_ID))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, roles = "CANDIDATE")
    @DisplayName("Should get empty my applications as CANDIDATE")
    void getMyApplications_asUser_returnsEmpty() throws Exception {
        List<EntityModel<JobApplicationResponseDTO>> emptyContent = List.of();
        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(20, 0, 0, 0);
        PagedModel<EntityModel<JobApplicationResponseDTO>> emptyPaged = PagedModel.of(emptyContent, metadata);

        when(jobApplicationService.getApplicationsByCandidate(eq(TEST_USERNAME), eq(pageable)))
                .thenReturn(emptyPaged);

        mockMvc.perform(get("/api/applications/my-applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.page.totalElements").value(0))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    @DisplayName("Should update status if authorized")
    void updateStatus_asAuthorizedUser_returnsUpdatedApplication() throws Exception {
        jobApplication.setStatus(ApplicationStatus.REJECTED);
        JobApplicationResponseDTO updated = createJobApplicationResponseDTO(jobApplication);
        when(jobApplicationService.updateApplicationStatus(eq(TEST_ID), eq(ApplicationStatus.REJECTED)))
                .thenReturn(updated);
        when(jobSecurityService.canUpdateApplication(eq(TEST_ID), eq(ApplicationStatus.REJECTED), any(Authentication.class)))
                .thenReturn(true);

        mockMvc.perform(patch("/api/applications/{appId}/status", TEST_ID)
                        .with(csrf())
                        .param("status", "REJECTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    @WithMockUser
    @DisplayName("Returns 400 Bad Request when invalid non-numeric ID")
    void updateStatus_withNonNumericId_returnsBadRequest() throws Exception {
        String nonNumericId = "abc123";

        mockMvc.perform(patch("/api/applications/{appId}/status", nonNumericId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("status", "UNDER_REVIEW"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].code").value("TYPE_MISMATCH"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 403 for unauthorized status update")
    void updateStatus_unauthorized_shouldReturnForbidden() throws Exception {
        when(jobSecurityService.canUpdateApplication(eq(TEST_ID), eq(ApplicationStatus.UNDER_REVIEW), any(Authentication.class)))
                .thenReturn(false);

        mockMvc.perform(patch("/api/applications/{appId}/status", TEST_ID)
                        .with(csrf())
                        .param("status", "UNDER_REVIEW"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 for application not found in status update")
    void updateStatus_applicationNotFound_shouldReturnNotFound() throws Exception {
        when(jobApplicationService.updateApplicationStatus(eq(NON_EXISTENT_ID), eq(ApplicationStatus.UNDER_REVIEW)))
                .thenThrow(new ApiException("Application with id " + NON_EXISTENT_ID + " not found", HttpStatus.NOT_FOUND, "APPLICATION_NOT_FOUND"));
        when(jobSecurityService.canUpdateApplication(eq(NON_EXISTENT_ID), eq(ApplicationStatus.UNDER_REVIEW), any(Authentication.class)))
                .thenReturn(true);

        mockMvc.perform(patch("/api/applications/{appId}/status", NON_EXISTENT_ID)
                        .with(csrf())
                        .param("status", "UNDER_REVIEW"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].code").value("APPLICATION_NOT_FOUND"))
                .andExpect(jsonPath("$.errors[0].message").value("Application with id " + NON_EXISTENT_ID + " not found"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

}
