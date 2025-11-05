package com.github.kzhunmax.jobsearch.job.controller;

import com.github.kzhunmax.jobsearch.job.dto.JobRequestDTO;
import com.github.kzhunmax.jobsearch.job.dto.JobResponseDTO;
import com.github.kzhunmax.jobsearch.job.model.es.JobDocument;
import com.github.kzhunmax.jobsearch.job.service.JobService;
import com.github.kzhunmax.jobsearch.job.service.search.JobSearchService;
import com.github.kzhunmax.jobsearch.payload.ApiResponse;
import com.github.kzhunmax.jobsearch.security.RateLimitingService;
import com.github.kzhunmax.jobsearch.security.UserDetailsImpl;
import com.github.kzhunmax.jobsearch.user.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Jobs", description = "Job posting management and browsing")
public class JobController {
    private final JobService jobService;
    private final JobSearchService jobSearchService;
    private final RateLimitingService rateLimitingService;

    @PreAuthorize("hasRole('RECRUITER')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Create a new job posting",
            description = "Create a new job vacancy (recruiters only)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Job created successfully",
                    useReturnTypeSchema = true
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid job data",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - recruiter role required",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    public ResponseEntity<ApiResponse<JobResponseDTO>> createJob(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Job posting details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = JobRequestDTO.class))
            )
            @Valid @RequestBody JobRequestDTO dto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getId();
        log.info("Creating job - userId={}", userId);
        JobResponseDTO job = jobService.createJob(dto, userId);
        log.info("Job created successfully - userId={}", userId);
        return ApiResponse.created(job);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "List all active jobs",
            description = "Get paginated list of all active job postings"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Jobs retrieved successfully",
                    useReturnTypeSchema = true
            )
    })
    public ResponseEntity<ApiResponse<PagedModel<EntityModel<JobResponseDTO>>>> listJobs(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            PagedResourcesAssembler<JobResponseDTO> pagedAssembler
    ) {
        log.info("Listing all active jobs - pageable={}", pageable);
        PagedModel<EntityModel<JobResponseDTO>> jobs = jobService.getAllActiveJobs(pageable, pagedAssembler);
        log.info("Active jobs listed successfully");
        return ApiResponse.success(jobs);
    }

    @GetMapping(value = "/{jobId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get job by ID",
            description = "Retrieve detailed information about a specific job"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Job found successfully",
                    useReturnTypeSchema = true
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Job not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    public ResponseEntity<ApiResponse<JobResponseDTO>> getJobById(
            @Parameter(description = "ID of the job", example = "1")
            @PathVariable Long jobId) {
        log.info("Getting job by ID - jobId={}", jobId);
        JobResponseDTO job = jobService.getJobById(jobId);
        log.info("Job retrieved successfully - jobId={}", jobId);
        return ApiResponse.success(job);
    }

    @PreAuthorize("@jobSecurityService.isJobOwner(#jobId, authentication)")
    @PutMapping(value = "/{jobId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Update job posting",
            description = "Update an existing job posting (job owner only)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Job updated successfully",
                    useReturnTypeSchema = true
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - not the job owner",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Job not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
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
        log.info("Updating job - jobId={}", jobId);
        JobResponseDTO updatedJob = jobService.updateJob(jobId, dto);
        log.info("Job updated successfully - jobId={}", jobId);
        return ApiResponse.success(updatedJob);
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
                    description = "Job deleted successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - not the job owner",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Job not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    public ResponseEntity<ApiResponse<Void>> deleteJob(
            @Parameter(description = "ID of the job to delete", example = "1")
            @PathVariable Long jobId) {
        log.info("Deleting job - jobId={}", jobId);
        jobService.deleteJob(jobId);
        log.info("Job deleted successfully - jobId={}", jobId);
        return ApiResponse.noContent();
    }

    @PreAuthorize("hasRole('RECRUITER')")
    @GetMapping(value = "/my-jobs", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get my job postings",
            description = "Retrieve all job postings created by the current recruiter"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Jobs retrieved successfully",
            useReturnTypeSchema = true
    )
    public ResponseEntity<ApiResponse<PagedModel<EntityModel<JobResponseDTO>>>> getMyJobs(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PageableDefault(size = 20) Pageable pageable,
            PagedResourcesAssembler<JobResponseDTO> pagedAssembler) {
        Long userId = userDetails.getId();
        log.info("Getting my jobs - userId={}, pageable={}", userId, pageable);
        PagedModel<EntityModel<JobResponseDTO>> jobs = jobService.getJobsByRecruiter(userId, pageable, pagedAssembler);
        log.info("My jobs retrieved successfully - userId={}", userId);
        return ApiResponse.success(jobs);
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Search jobs by query",
            description = "Search active jobs by keyword in title/description, optionally filtered by location/company"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Search results retrieved",
                    useReturnTypeSchema = true
            )
    })
    public ResponseEntity<ApiResponse<PagedModel<EntityModel<JobDocument>>>> searchJobs(
            @Parameter(description = "Search keyword", example = "Java") @RequestParam String query,
            @Parameter(description = "Optional location filter", example = "Remote") @RequestParam(required = false) String location,
            @Parameter(description = "Optional company filter", example = "TechCorp") @RequestParam(required = false) String company,
            @PageableDefault(size = 20) Pageable pageable,
            PagedResourcesAssembler<JobDocument> pagedAssembler,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        User user = userDetails.getUser();
        rateLimitingService.consumeToken(user.getApiKey(), user.getPricingPlan(), "API_KEY");
        log.info("Searching jobs - query={}, location={}, company={}", query, location, company);
        PagedModel<EntityModel<JobDocument>> results = jobSearchService.searchJobs(query, location, company, pageable, pagedAssembler);
        log.info("Search completed with total of - {} results", results.getMetadata() != null ? results.getMetadata().getTotalElements() : 0);
        return ApiResponse.success(results);
    }

}
