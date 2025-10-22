package com.github.kzhunmax.jobsearch.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kzhunmax.jobsearch.dto.request.JobApplicationRequestDTO;
import com.github.kzhunmax.jobsearch.dto.response.JobApplicationResponseDTO;
import com.github.kzhunmax.jobsearch.model.ApplicationStatus;
import com.github.kzhunmax.jobsearch.payload.ApiResponse;
import com.github.kzhunmax.jobsearch.service.JobApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.github.kzhunmax.jobsearch.constants.LoggingConstants.REQUEST_ID_MDC_KEY;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Job Applications", description = "Manage job applications and application status")
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/apply/{jobId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(
            summary = "Apply to a job",
            description = "Submit a job application for the specified job position, including cover letter and CV (PDF) upload"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Application submitted successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "data": {
                                                "id": 1,
                                                "jobId": 1,
                                                "jobTitle": "Java Developer",
                                                "company": "TechCorp",
                                                "candidateEmail": "user@example.com",
                                                "status": "APPLIED",
                                                "appliedAt": "2025-09-22T10:15:30Z",
                                                "coverLetter": "Some text in cover letter",
                                                "cvUrl": "https://your-supabase-url/storage/v1/object/public/cvs/candidates/user_example_com/uuid_filename.pdf"
                                              },
                                              "errors": [],
                                              "timestamp": "2025-09-22T10:15:30Z",
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
                                                  "code": "INVALID_CV",
                                                  "message": "CV must be a non-empty PDF file"
                                                }
                                              ],,
                                              "timestamp": "2025-09-22T10:15:30Z",
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
                                                  "message": "Job with id 3 not found"
                                                }
                                              ],
                                              "timestamp": "2025-09-22T10:15:30Z",
                                              "requestId": "request-123"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Already applied to this job",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "data": null,
                                              "errors": [
                                                {
                                                  "code": "DUPLICATE_APPLICATION",
                                                  "message": "User has already applied to this job"
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
    public ResponseEntity<ApiResponse<JobApplicationResponseDTO>> apply(
            @Parameter(description = "ID of the job to apply for", example = "1")
            @PathVariable Long jobId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Job application details including cover letter",
                    required = true,
                    content = @Content(schema = @Schema(implementation = JobApplicationRequestDTO.class))
            )
            @RequestPart("request") String requestJson,
            @Parameter(description = "CV file (PDF, max 5MB) - form part named 'cv'", required = true)
            @RequestPart("resume") MultipartFile resume,
            Authentication authentication) {
        String email = authentication.getName();
        try {
            JobApplicationRequestDTO requestDto = objectMapper.readValue(requestJson, JobApplicationRequestDTO.class);
            log.info("User '{}' is applying to job with id={} | coverLetterLength={}", email, jobId, requestDto.coverLetter().length());
            JobApplicationResponseDTO responseDto = jobApplicationService.applyToJob(jobId, email, requestDto.coverLetter(), resume);
            log.info("User '{}' successfully applied to job id={} | applicationId={}", email, jobId, responseDto.id());
            return ApiResponse.success(responseDto, MDC.get(REQUEST_ID_MDC_KEY));
        } catch (Exception e) {
            log.error("Failed to parse request JSON for user {}: {}", email, e.getMessage());
            throw new IllegalArgumentException("Invalid JSON in request part: " + e.getMessage());
        }
    }
    @GetMapping("/job/{jobId}")
    @PreAuthorize("@jobSecurityService.isJobOwner(#jobId, authentication) or hasRole('ADMIN')")
    @Operation(
            summary = "Get applications for a job",
            description = "Retrieve all applications for a specific job (recruiters and admins only)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Applications retrieved successfully",
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
                                                    "href": "http://localhost:8080/api/applications/job/1?page=0&size=20"
                                                  }
                                                ],
                                                "content": [
                                                  {
                                                    "id": 1,
                                                    "jobId": 1,
                                                    "jobTitle": "Java Developer",
                                                    "company": "TechCorp",
                                                    "candidateEmail": "user@example",
                                                    "status": "APPLIED",
                                                    "appliedAt": "2025-09-22T10:15:30Z",
                                                    "coverLetter": "CV",
                                                    "cvUrl": "https://your-supabase-url/storage/v1/object/public/cvs/candidates/user_example_com/uuid_filename.pdf",
                                                    "links": []
                                                  }
                                                ],
                                                "page": {
                                                  "size": 20,
                                                  "totalElements": 1,
                                                  "totalPages": 1,
                                                  "number": 0
                                                }
                                              },
                                              "timestamp": "2025-09-22T10:15:30Z",
                                              "requestId": "request-123"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - not the job owner or admin",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2025-09-30T20:17:27.866+00:00",
                                              "status": 403,
                                              "error": "Forbidden",
                                              "path": "/api/applications/job/1"
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
                                                  "message": "Job with id 2 not found"
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
    public ResponseEntity<ApiResponse<PagedModel<EntityModel<JobApplicationResponseDTO>>>> getApplicationForJob(
            @Parameter(description = "ID of the job", example = "1")
            @PathVariable Long jobId,
            Pageable pageable,
            PagedResourcesAssembler<JobApplicationResponseDTO> pagedAssembler) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Fetching applications for jobId={} with pageable={}", requestId, jobId, pageable);
        PagedModel<EntityModel<JobApplicationResponseDTO>> applications = jobApplicationService.getApplicationsForJob(jobId, pageable, pagedAssembler);
        int total = applications.getMetadata() != null
                ? (int) applications.getMetadata().getTotalElements()
                : applications.getContent().size();

        log.info("Request [{}]: Found {} applications for jobId={}", requestId, total, jobId);
        return ApiResponse.success(applications, requestId);
    }

    @GetMapping("/my-applications")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(
            summary = "Get my applications",
            description = "Retrieve all job applications submitted by the current user"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Applications retrieved successfully",
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
                                            "href": "http://localhost:8080/api/applications/my-applications?page=0&size=20"
                                          }
                                        ],
                                        "content": [
                                        {
                                            "id": 1,
                                            "jobId": 1,
                                            "jobTitle": "Java Developer",
                                            "company": "TechCorp",
                                            "status": "APPLIED",
                                            "appliedAt": "2025-09-22T10:15:30Z",
                                            "coverLetter": "CV",
                                            "cvUrl": "https://your-supabase-url/storage/v1/object/public/cvs/candidates/user_example_com/uuid_filename.pdf",
                                            "links": []
                                          }
                                        ],
                                        "page": {
                                          "size": 20,
                                          "totalElements": 1,
                                          "totalPages": 1,
                                          "number": 0
                                        }
                                      },
                                      "errors": [],
                                      "timestamp": "2025-09-22T10:15:30Z",
                                      "requestId": "request-123"
                                    }
                                    """
                    )
            )
    )
    public ResponseEntity<ApiResponse<PagedModel<EntityModel<JobApplicationResponseDTO>>>> getMyApplications(
            Authentication authentication,
            Pageable pageable,
            PagedResourcesAssembler<JobApplicationResponseDTO> pagedAssembler
    ) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        String email = authentication.getName();
        log.info("Request [{}]: Fetching applications for candidate='{}' with pageable={}", requestId, email, pageable);
        PagedModel<EntityModel<JobApplicationResponseDTO>> applications = jobApplicationService.getApplicationsByCandidate(email, pageable, pagedAssembler);
        int total = applications.getMetadata() != null
                ? (int) applications.getMetadata().getTotalElements()
                : applications.getContent().size();

        log.info("Request [{}]: Found {} applications for candidate='{}'", requestId, total, email);
        return ApiResponse.success(applications, requestId);
    }

    @PatchMapping("/{appId}/status")
    @PreAuthorize("@jobSecurityService.canUpdateApplication(#appId, #status, authentication)")
    @Operation(
            summary = "Update application status",
            description = "Update the status of a job application (recruiters and admins only)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Status updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "data": {
                                                "id": 1,
                                                "jobId": 1,
                                                "jobTitle": "Java Developer",
                                                "company": "TechCorp",
                                                "candidateEmail": "user@example.com",
                                                "status": "UNDER_REVIEW",
                                                "appliedAt": "2025-09-30T17:31:57.448674Z",
                                                "coverLetter": "CV",
                                                "cvUrl": "https://your-supabase-url/storage/v1/object/public/cvs/candidates/user_example_com/uuid_filename.pdf"
                                              },
                                              "errors": [],
                                              "timestamp": "2025-09-22T10:15:30Z",
                                              "requestId": "request-123"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - not authorized to update this application",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2025-09-30T20:22:40.565+00:00",
                                              "status": 403,
                                              "error": "Forbidden",
                                              "path": "/api/applications/1/status"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Application not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "data": null,
                                              "errors": [
                                                {
                                                  "code": "APPLICATION_NOT_FOUND",
                                                  "message": "Application with id -1 not found"
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
    public ResponseEntity<ApiResponse<JobApplicationResponseDTO>> updateStatus(
            @Parameter(description = "ID of the application", example = "1")
            @PathVariable Long appId,

            @Parameter(
                    description = "New application status",
                    example = "UNDER_REVIEW",
                    schema = @Schema(implementation = ApplicationStatus.class)
            )
            @RequestParam ApplicationStatus status) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Update status for application={} to {}", requestId, appId, status);
        JobApplicationResponseDTO updatedApplication = jobApplicationService.updateApplicationStatus(appId, status);
        log.info("Request [{}]: Application status updated successfully | applicationId={} | newStatus={}",
                requestId, updatedApplication.id(), updatedApplication.status());
        return ApiResponse.success(updatedApplication, requestId);
    }
}
