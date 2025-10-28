package com.github.kzhunmax.jobsearch.job.controller;

import com.github.kzhunmax.jobsearch.job.dto.JobRequestDTO;
import com.github.kzhunmax.jobsearch.job.dto.JobResponseDTO;
import com.github.kzhunmax.jobsearch.job.model.es.JobDocument;
import com.github.kzhunmax.jobsearch.job.service.JobService;
import com.github.kzhunmax.jobsearch.job.service.search.JobSearchService;
import com.github.kzhunmax.jobsearch.payload.ApiResponse;
import com.github.kzhunmax.jobsearch.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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

import static com.github.kzhunmax.jobsearch.constants.LoggingConstants.REQUEST_ID_MDC_KEY;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Jobs", description = "Job posting management and browsing")
public class JobController {
    private final JobService jobService;
    private final JobSearchService jobSearchService;

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
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        Long userId = userDetails.getId();
        log.info("Request [{}]: Creating job - userId={}", requestId, userId);
        JobResponseDTO job = jobService.createJob(dto, userId);
        log.info("Request [{}]: Job created successfully - userId={}", requestId, userId);
        return ApiResponse.created(job, requestId);
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
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Listing all active jobs - pageable={}", requestId, pageable);
        PagedModel<EntityModel<JobResponseDTO>> jobs = jobService.getAllActiveJobs(pageable, pagedAssembler);
        log.info("Request [{}]: Active jobs listed successfully", requestId);
        return ApiResponse.success(jobs, requestId);
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
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Getting job by ID - jobId={}", requestId, jobId);
        JobResponseDTO job = jobService.getJobById(jobId);
        log.info("Request [{}]: Job retrieved successfully - jobId={}", requestId, jobId);
        return ApiResponse.success(job, requestId);
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
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Updating job - jobId={}", requestId, jobId);
        JobResponseDTO updatedJob = jobService.updateJob(jobId, dto);
        log.info("Request [{}]: Job updated successfully - jobId={}", requestId, jobId);
        return ApiResponse.success(updatedJob, requestId);
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
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Deleting job - jobId={}", requestId, jobId);
        jobService.deleteJob(jobId);
        log.info("Request [{}]: Job deleted successfully - jobId={}", requestId, jobId);
        return ApiResponse.noContent(requestId);
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
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        Long userId = userDetails.getId();
        log.info("Request [{}]: Getting my jobs - userId={}, pageable={}", requestId, userId, pageable);
        PagedModel<EntityModel<JobResponseDTO>> jobs = jobService.getJobsByRecruiter(userId, pageable, pagedAssembler);
        log.info("Request [{}]: My jobs retrieved successfully - userId={}", requestId, userId);
        return ApiResponse.success(jobs, requestId);
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
            PagedResourcesAssembler<JobDocument> pagedAssembler
    ) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Searching jobs - query={}, location={}, company={}", requestId, query, location, company);
        PagedModel<EntityModel<JobDocument>> results = jobSearchService.searchJobs(query, location, company, pageable, pagedAssembler);
        log.info("Request [{}]: Search completed - {} results", requestId, results.getMetadata().getTotalElements());
        return ApiResponse.success(results, requestId);
    }

}
