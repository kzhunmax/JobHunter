package com.github.kzhunmax.jobsearch.controller;

import com.github.kzhunmax.jobsearch.dto.request.JobRequestDTO;
import com.github.kzhunmax.jobsearch.dto.response.JobResponseDTO;
import com.github.kzhunmax.jobsearch.payload.ApiResponse;
import com.github.kzhunmax.jobsearch.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static com.github.kzhunmax.jobsearch.constants.LoggingConstants.REQUEST_ID_MDC_KEY;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
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
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "data": {
                                                "id": 1,
                                                "title": "Java Developer",
                                                "description": "Looking for experienced Java developer with Spring Boot knowledge",
                                                "company": "TechCorp",
                                                "location": "Remote",
                                                "salary": 1020000,
                                                "active": true,
                                                "postedBy": "recruiter"
                                              },
                                              "errors": [],
                                              "timestamp": "2025-09-29T20:00:28.786132065Z",
                                              "requestId": "request-123"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid job data",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "data": null,
                                              "errors": [
                                                {
                                                  "code": "VALIDATION_FAILED",
                                                  "message": "Validation failed"
                                                }
                                              ],
                                              "timestamp": "2025-10-01T19:48:47.010535588Z",
                                              "requestId": "request-123"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - recruiter role required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2025-10-01T19:50:53.258+00:00",
                                              "status": 403,
                                              "error": "Forbidden",
                                              "path": "/api/jobs"
                                            }
                                            """
                            )
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
            Authentication authentication
    ) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        String username = authentication.getName();
        log.info("Request [{}]: Creating job - username={}", requestId, username);
        JobResponseDTO job = jobService.createJob(dto, username);
        log.info("Request [{}]: Job created successfully - username={}", requestId, username);
        return ApiResponse.created(job, requestId);
    }

    @GetMapping
    @Operation(
            summary = "List all active jobs",
            description = "Get paginated list of all active job postings"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Jobs retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "data": {
                                        "links": [
                                          {
                                            "rel": "self",
                                            "href": "http://localhost:8080/api/jobs?page=0&size=20&sort=createdAt,asc"
                                          }
                                        ],
                                        "content": [
                                          {
                                            "id": 1,
                                            "title": "Java Developer",
                                            "description": "Looking for experienced Java developer with Spring Boot knowledge",
                                            "company": "TechCorp",
                                            "location": "Remote",
                                            "salary": 1020000,
                                            "active": true,
                                            "postedBy": "recruiter",
                                            "links": []
                                          },
                                          {
                                            "id": 2,
                                            "title": "Java Developer",
                                            "description": "Looking for experienced Java developer with Spring Boot knowledge",
                                            "company": "TechCorp",
                                            "location": "Remote",
                                            "salary": 1020000,
                                            "active": true,
                                            "postedBy": "recruiter",
                                            "links": []
                                          }
                                        ],
                                        "page": {
                                          "size": 20,
                                          "totalElements": 2,
                                          "totalPages": 1,
                                          "number": 0
                                        }
                                      },
                                      "errors": [],
                                      "timestamp": "2025-10-01T20:11:13.869231411Z",
                                      "requestId": "request-123"
                                    }
                                    """
                    )
            )
    )
    public ResponseEntity<ApiResponse<PagedModel<EntityModel<JobResponseDTO>>>> listJobs(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Listing all active jobs - pageable={}", requestId, pageable);
        PagedModel<EntityModel<JobResponseDTO>> jobs = jobService.getAllActiveJobs(pageable);
        log.info("Request [{}]: Active jobs listed successfully", requestId);
        return ApiResponse.success(jobs, requestId);
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
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "data": {
                                                "id": 1,
                                                "title": "Java Developer",
                                                "description": "Looking for experienced Java developer with Spring Boot knowledge",
                                                "company": "TechCorp",
                                                "location": "Remote",
                                                "salary": 1020000,
                                                "active": true,
                                                "postedBy": "recruiter"
                                              },
                                              "errors": [],
                                              "timestamp": "2025-10-01T20:15:54.924550574Z",
                                              "requestId": "request-123"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Job not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "data": null,
                                              "errors": [
                                                {
                                                  "code": "JOB_NOT_FOUND",
                                                  "message": "Job with id -1 not found"
                                                }
                                              ],
                                              "timestamp": "2025-09-22T10:15:30Z",
                                              "requestId": "request-123"
                                            }
                                            """
                            )
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
    @PutMapping("/{jobId}")
    @Operation(
            summary = "Update job posting",
            description = "Update an existing job posting (job owner only)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Job updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "data": {
                                                "id": 1,
                                                "title": "Python Developer",
                                                "description": "Updated description with more requirements",
                                                "company": "TechCorp Inc",
                                                "location": "Hybrid",
                                                "salary": 130000,
                                                "active": true,
                                                "postedBy": "recruiter"
                                              },
                                              "errors": [],
                                              "timestamp": "2025-10-01T20:17:27.613225110Z",
                                              "requestId": "request-123"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - not the job owner",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2025-10-01T20:18:44.815+00:00",
                                              "status": 403,
                                              "error": "Forbidden",
                                              "path": "/api/jobs/1"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Job not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "data": null,
                                              "errors": [
                                                {
                                                  "code": "JOB_NOT_FOUND",
                                                  "message": "Job with id -1 not found"
                                                }
                                              ],
                                              "timestamp": "2025-10-01T20:16:50.059555085Z",
                                              "requestId": "request-123"
                                            }
                                            """
                            )
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
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - not the job owner",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Job not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "data": null,
                                              "errors": [
                                                {
                                                  "code": "JOB_NOT_FOUND",
                                                  "message": "Job with id -1 not found"
                                                }
                                              ],
                                              "timestamp": "2025-10-01T20:16:50.059555085Z",
                                              "requestId": "request-123"
                                            }
                                            """
                            )
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
    @GetMapping("/my-jobs")
    @Operation(
            summary = "Get my job postings",
            description = "Retrieve all job postings created by the current recruiter"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Jobs retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "data": {
                                        "links": [
                                          {
                                            "rel": "self",
                                            "href": "http://localhost:8080/api/jobs?page=0&size=20&sort=createdAt,asc"
                                          }
                                        ],
                                        "content": [
                                          {
                                            "id": 1,
                                            "title": "Java Developer",
                                            "description": "Looking for experienced Java developer with Spring Boot knowledge",
                                            "company": "TechCorp",
                                            "location": "Remote",
                                            "salary": 1020000,
                                            "active": true,
                                            "postedBy": "recruiter",
                                            "links": []
                                          },
                                          {
                                            "id": 2,
                                            "title": "Java Developer",
                                            "description": "Looking for experienced Java developer with Spring Boot knowledge",
                                            "company": "TechCorp",
                                            "location": "Remote",
                                            "salary": 1020000,
                                            "active": true,
                                            "postedBy": "recruiter",
                                            "links": []
                                          }
                                        ],
                                        "page": {
                                          "size": 20,
                                          "totalElements": 2,
                                          "totalPages": 1,
                                          "number": 0
                                        }
                                      },
                                      "errors": [],
                                      "timestamp": "2025-10-01T20:11:13.869231411Z",
                                      "requestId": "request-123"
                                    }
                                    """
                    )
            )
    )
    public ResponseEntity<ApiResponse<PagedModel<EntityModel<JobResponseDTO>>>> getMyJobs(
            Authentication authentication,
            @PageableDefault(size = 20) Pageable pageable) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        String username = authentication.getName();
        log.info("Request [{}]: Getting my jobs - username={}, pageable={}", requestId, username, pageable);
        PagedModel<EntityModel<JobResponseDTO>> jobs = jobService.getJobsByRecruiter(username, pageable);
        log.info("Request [{}]: My jobs retrieved successfully - username={}", requestId, username);
        return ApiResponse.success(jobs, requestId);
    }
}
