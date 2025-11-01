package com.github.kzhunmax.jobsearch.job.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kzhunmax.jobsearch.job.dto.JobApplicationRequestDTO;
import com.github.kzhunmax.jobsearch.job.dto.JobApplicationResponseDTO;
import com.github.kzhunmax.jobsearch.job.service.JobApplicationService;
import com.github.kzhunmax.jobsearch.payload.ApiResponse;
import com.github.kzhunmax.jobsearch.security.UserDetailsImpl;
import com.github.kzhunmax.jobsearch.shared.enums.ApplicationStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @PostMapping(value = "/apply/{jobId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(
            summary = "Apply to a job",
            description = "Submit a job application for the specified job position, including cover letter and CV (PDF) upload"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Application submitted successfully",
                    useReturnTypeSchema = true
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Job not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Already applied to this job",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
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
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getId();
        try {
            JobApplicationRequestDTO requestDto = objectMapper.readValue(requestJson, JobApplicationRequestDTO.class);
            log.info("User '{}' is applying to job with id={} | coverLetterLength={}", userId, jobId, requestDto.coverLetter().length());
            JobApplicationResponseDTO responseDto = jobApplicationService.applyToJob(jobId, userId, requestDto.coverLetter(), resume);
            log.info("User '{}' successfully applied to job id={} | applicationId={}", userId, jobId, responseDto.id());
            return ApiResponse.success(responseDto, MDC.get(REQUEST_ID_MDC_KEY));
        } catch (JsonProcessingException e) {
            log.error("Failed to parse request JSON for user {}: {}", userId, e.getMessage());
            throw new IllegalArgumentException("Invalid JSON in request part: " + e.getMessage());
        }
    }

    @GetMapping(value = "/job/{jobId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("@jobSecurityService.isJobOwner(#jobId, authentication) or hasRole('ADMIN')")
    @Operation(
            summary = "Get applications for a job",
            description = "Retrieve all applications for a specific job (recruiters and admins only)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Applications retrieved successfully",
                    useReturnTypeSchema = true
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - not the job owner or admin",
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

    @GetMapping(value = "/my-applications", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(
            summary = "Get my applications",
            description = "Retrieve all job applications submitted by the current user"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Applications retrieved successfully",
                    useReturnTypeSchema = true
            )
    })
    public ResponseEntity<ApiResponse<PagedModel<EntityModel<JobApplicationResponseDTO>>>> getMyApplications(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            Pageable pageable,
            PagedResourcesAssembler<JobApplicationResponseDTO> pagedAssembler
    ) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        Long userId = userDetails.getId();
        log.info("Request [{}]: Fetching applications for candidate='{}' with pageable={}", requestId, userId, pageable);
        PagedModel<EntityModel<JobApplicationResponseDTO>> applications = jobApplicationService.getApplicationsByCandidate(userId, pageable, pagedAssembler);
        int total = applications.getMetadata() != null
                ? (int) applications.getMetadata().getTotalElements()
                : applications.getContent().size();

        log.info("Request [{}]: Found {} applications for candidate='{}'", requestId, total, userId);
        return ApiResponse.success(applications, requestId);
    }

    @PatchMapping(value = "/{appId}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("@jobSecurityService.canUpdateApplication(#appId, #status, authentication)")
    @Operation(
            summary = "Update application status",
            description = "Update the status of a job application (recruiters and admins only)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Status updated successfully",
                    useReturnTypeSchema = true
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - not authorized to update this application",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Application not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
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
