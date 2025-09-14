package com.github.kzhunmax.jobsearch.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kzhunmax.jobsearch.dto.request.JobRequestDTO;
import com.github.kzhunmax.jobsearch.dto.response.JobResponseDTO;
import com.github.kzhunmax.jobsearch.security.JwtService;
import com.github.kzhunmax.jobsearch.service.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static com.github.kzhunmax.jobsearch.util.TestDataFactory.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private JobRequestDTO validJobRequest;
    private JobResponseDTO jobResponse;
    private JobRequestDTO invalidJobRequest;

    @BeforeEach
    void setUp() {
        validJobRequest = createJobRequest();
        jobResponse = createJobResponse(1L);
        invalidJobRequest = createInvalidJobRequest();
    }

    @Nested
    @DisplayName("Create Job Endpoint Tests")
    class CreateJob {
        @Test
        @DisplayName("Returns created job when request is valid and user has recruiter role")
        @WithMockUser(username = "recruiter", roles = "RECRUITER")
        void withValidRequestAndRecruiterRole_returnCreatedJob() throws Exception {
            when(jobService.createJob(any(JobRequestDTO.class), eq("recruiter"))).thenReturn(jobResponse);

            mockMvc.perform(post("/api/jobs")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validJobRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.id").value(1L))
                    .andExpect(jsonPath("$.data.title").value("Java Dev"))
                    .andExpect(jsonPath("$.data.company").value("BigTech"))
                    .andExpect(jsonPath("$.data.location").value("Remote"))
                    .andExpect(jsonPath("$.data.salary").value(5000.0));
        }

        @Test
        @DisplayName("Returns 403 Forbidden when user does not have recruiter role")
        @WithMockUser(username = "user", roles = "USER")
        void withUnauthorizedUserRole_returnsForbidden() throws Exception {
            mockMvc.perform(post("/api/jobs")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validJobRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Returns created job when request is valid and user has recruiter role")
        @WithMockUser(username = "recruiter", roles = "RECRUITER")
        void withInvalidRequest_returnsBadRequest() throws Exception {
            when(jobService.createJob(any(JobRequestDTO.class), eq("recruiter"))).thenReturn(jobResponse);

            mockMvc.perform(post("/api/jobs")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidJobRequest)))
                    .andExpect(status().isBadRequest());
        }
    }
}
