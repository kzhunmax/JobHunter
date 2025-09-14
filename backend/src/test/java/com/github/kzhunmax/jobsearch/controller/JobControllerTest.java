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
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static com.github.kzhunmax.jobsearch.util.TestDataFactory.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Nested
    @WithMockUser(username = "user", roles = "USER")
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
                    new PagedModel.PageMetadata(2, 0, 2)
            );

            when(jobService.getAllActiveJobs(any(Pageable.class))).thenReturn(pagedJobs);

            mockMvc.perform(get("/api/jobs")
                            .param("page", "0")
                            .param("size", "20")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.page.size").value(2))
                    .andExpect(jsonPath("$.data.page.totalPages").value(1))
                    .andExpect(jsonPath("$.data.page.totalElements").value(2));
        }

        @Test
        @DisplayName("Returns empty list when no active jobs")
        void withNoActiveJobs_returnsEmptyList() throws Exception {
            PagedModel<EntityModel<JobResponseDTO>> emptyPagedJobs = PagedModel.empty();
            when(jobService.getAllActiveJobs(any(Pageable.class))).thenReturn(emptyPagedJobs);

            mockMvc.perform(get("/api/jobs")
                            .param("page", "0")
                            .param("size", "20")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isEmpty());
        }

        @Test
        @DisplayName("Support pagination parameters")
        void withPagination_returnsCorrectPage() throws Exception {
            List<JobResponseDTO> jobList = List.of(
                    createJobResponse(3L)
            );

            PagedModel<EntityModel<JobResponseDTO>> pagedJobs = PagedModel.of(
                    jobList.stream().map(EntityModel::of).toList(),
                    new PagedModel.PageMetadata(1, 0, 3)
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
                    .andExpect(jsonPath("$.data.page.number").value(0))
                    .andExpect(jsonPath("$.data.page.totalElements").value(3));
        }
    }
}
