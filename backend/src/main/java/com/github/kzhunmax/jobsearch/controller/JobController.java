package com.github.kzhunmax.jobsearch.controller;

import com.github.kzhunmax.jobsearch.dto.request.JobRequestDTO;
import com.github.kzhunmax.jobsearch.dto.response.JobResponseDTO;
import com.github.kzhunmax.jobsearch.payload.ApiResponse;
import com.github.kzhunmax.jobsearch.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Tag(name = "Jobs", description = "Job posting management and browsing")
public class JobController {
    private final JobService jobService;

    @PreAuthorize("hasRole('RECRUITER')")
    @PostMapping
    @Operation(
            summary = "Create a new job posting",
            description = "Create a new job vacancy (recruiters only)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Job created successfully",
                    content = @Content(schema = @Schema(implementation = JobResponseDTO.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid job data"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - recruiter role required"
            )
    })
    public ResponseEntity<ApiResponse<JobResponseDTO>> createJob(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Job posting details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = JobRequestDTO.class))
            )
            @Valid @RequestBody JobRequestDTO dto,
            Authentication authentication
    ) {
        String username = authentication.getName();
        JobResponseDTO job = jobService.createJob(dto, username);
        return ApiResponse.created(job, MDC.get("requestId"));
    }

    @GetMapping
    @Operation(
            summary = "List all active jobs",
            description = "Get paginated list of all active job postings"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Jobs retrieved successfully"
    )
    public ResponseEntity<ApiResponse<PagedModel<EntityModel<JobResponseDTO>>>> listJobs(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        PagedModel<EntityModel<JobResponseDTO>> jobs = jobService.getAllActiveJobs(pageable);
        return ApiResponse.success(jobs, MDC.get("requestId"));
    }

    @GetMapping("/{jobId}")
    @Operation(
            summary = "Get job by ID",
            description = "Retrieve detailed information about a specific job"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Job found successfully",
                    content = @Content(schema = @Schema(implementation = JobResponseDTO.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Job not found"
            )
    })
    public ResponseEntity<ApiResponse<JobResponseDTO>> getJobById(
            @Parameter(description = "ID of the job", example = "1")
            @PathVariable Long jobId) {
        JobResponseDTO job = jobService.getJobById(jobId);
        return ApiResponse.success(job, MDC.get("requestId"));
    }

    @PreAuthorize("@jobSecurityService.isJobOwner(#jobId, authentication)")
    @PutMapping("/{jobId}")
    @Operation(
            summary = "Update job posting",
            description = "Update an existing job posting (job owner only)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Job updated successfully",
                    content = @Content(schema = @Schema(implementation = JobResponseDTO.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - not the job owner"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Job not found"
            )
    })
    public ResponseEntity<ApiResponse<JobResponseDTO>> updateJob(
            @Parameter(description = "ID of the job to update", example = "1")
            @PathVariable Long jobId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated job details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = JobRequestDTO.class))
            )
            @Valid @RequestBody JobRequestDTO dto) {
        JobResponseDTO updatedJob = jobService.updateJob(jobId, dto);
        return ApiResponse.success(updatedJob, MDC.get("requestId"));
    }

    @PreAuthorize("@jobSecurityService.isJobOwner(#jobId, authentication)")
    @DeleteMapping("/{jobId}")
    @Operation(
            summary = "Delete job posting",
            description = "Delete a job posting (job owner only)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "Job deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - not the job owner"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Job not found"
            )
    })
    public ResponseEntity<ApiResponse<Void>> deleteJob(
            @Parameter(description = "ID of the job to delete", example = "1")
            @PathVariable Long jobId) {
        jobService.deleteJob(jobId);
        return ApiResponse.noContent(MDC.get("requestId"));
    }

    @PreAuthorize("hasRole('RECRUITER')")
    @GetMapping("/my-jobs")
    @Operation(
            summary = "Get my job postings",
            description = "Retrieve all job postings created by the current recruiter"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Jobs retrieved successfully"
    )
    public ResponseEntity<ApiResponse<PagedModel<EntityModel<JobResponseDTO>>>> getMyJobs(
            Authentication authentication,
            @PageableDefault(size = 20) Pageable pageable) {
        String username = authentication.getName();
        PagedModel<EntityModel<JobResponseDTO>> jobs = jobService.getJobsByRecruiter(username, pageable);
        return ApiResponse.success(jobs, MDC.get("requestId"));
    }
}
