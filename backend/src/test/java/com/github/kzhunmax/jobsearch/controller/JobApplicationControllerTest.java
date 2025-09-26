package com.github.kzhunmax.jobsearch.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kzhunmax.jobsearch.dto.request.JobApplicationRequestDTO;
import com.github.kzhunmax.jobsearch.dto.response.JobApplicationResponseDTO;
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

    @BeforeEach
    void setUp() {
        testUser = createUser(TEST_ID, TEST_USERNAME);
        testJob = createJob(TEST_ID, testUser, true);
        jobApplication = createJobApplication(TEST_ID, testUser, testJob);
        jobApplicationResponseDTO = createJobApplicationResponseDTO(jobApplication);
        jobApplicationRequestDTO = createJobApplicationRequestDTO(TEST_ID, "Cover Letter");
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, roles = "CANDIDATE")
    @DisplayName("Should apply for job as CANDIDATE")
    void applyForJob_asUser_returnsResponse() throws Exception {

        when(jobApplicationService.applyToJob(anyLong(), anyString(), anyString()))
                .thenReturn(jobApplicationResponseDTO);

        mockMvc.perform(post("/api/applications/apply/{jobId}", TEST_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jobApplicationRequestDTO)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(TEST_ID))
                .andExpect(jsonPath("$.data.jobId").value(TEST_ID))
                .andExpect(jsonPath("$.data.candidateUsername").value(TEST_USERNAME))
                .andExpect(jsonPath("$.data.status").value("APPLIED"));
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    @DisplayName("Should get my applications as CANDIDATE")
    void getMyApplications_asUser_returnsApplications() throws Exception {
        PagedModel<EntityModel<JobApplicationResponseDTO>> paged =
                PagedModel.of(List.of(EntityModel.of(jobApplicationResponseDTO)), new PagedModel.PageMetadata(1, 0, 1));

        when(jobApplicationService.getApplicationsByCandidate(anyString(), any(Pageable.class)))
                .thenReturn(paged);

        mockMvc.perform(get("/api/applications/my-applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(TEST_ID));
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
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    @Test
    @DisplayName("Should reject unauthenticated requests to /my-applications")
    void getMyApplications_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/applications/my-applications"))
                .andExpect(status().isUnauthorized());
    }
}
