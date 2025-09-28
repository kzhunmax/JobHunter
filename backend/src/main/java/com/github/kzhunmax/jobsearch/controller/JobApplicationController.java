package com.github.kzhunmax.jobsearch.controller;

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
import org.slf4j.MDC;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Tag(name = "Job Applications", description = "Manage job applications and application status")
public class JobApplicationController {

    private static final String REQUEST_ID_MDC_KEY = "requestId";

    private final JobApplicationService jobApplicationService;

    @PostMapping("/apply/{jobId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(
            summary = "Apply to a job",
            description = "Submit a job application for the specified job position"
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
                                                "candidateUsername": "user",
                                                "status": "APPLIED",
                                                "appliedAt": "2025-09-22T10:15:30Z",
                                                "coverLetter": "Some text in cover letter"
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
                    responseCode = "400",
                    description = "Invalid job ID or application data",
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
            @RequestBody JobApplicationRequestDTO requestDto, Authentication authentication) {
        String username = authentication.getName();
        JobApplicationResponseDTO responseDto = jobApplicationService.applyToJob(jobId, username, requestDto.coverLetter());
        return ApiResponse.success(responseDto, MDC.get(REQUEST_ID_MDC_KEY));
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
                                                    "candidateUsername": "user",
                                                    "status": "APPLIED",
                                                    "appliedAt": "2025-09-22T10:15:30Z",
                                                    "coverLetter": "CV",
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
                    description = "Access denied - not the job owner or admin"
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
            Pageable pageable) {
        PagedModel<EntityModel<JobApplicationResponseDTO>> applications = jobApplicationService.getApplicationsForJob(jobId, pageable);
        return ApiResponse.success(applications, MDC.get(REQUEST_ID_MDC_KEY));
    }

    @GetMapping("/my-applications")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(
            summary = "Get my applications",
            description = "Retrieve all job applications submitted by the current user"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Applications retrieved successfully"
    )
    public ResponseEntity<ApiResponse<PagedModel<EntityModel<JobApplicationResponseDTO>>>> getMyApplications(Authentication authentication, Pageable pageable) {
        String username = authentication.getName();
        PagedModel<EntityModel<JobApplicationResponseDTO>> applications = jobApplicationService.getApplicationsByCandidate(username, pageable);
        return ApiResponse.success(applications, MDC.get(REQUEST_ID_MDC_KEY));
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
                    content = @Content(schema = @Schema(implementation = JobApplicationResponseDTO.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - not authorized to update this application"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Application not found"
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
        JobApplicationResponseDTO updatedApplication = jobApplicationService.updateApplicationStatus(appId, status);
        return ApiResponse.success(updatedApplication, MDC.get(REQUEST_ID_MDC_KEY));
    }
}
