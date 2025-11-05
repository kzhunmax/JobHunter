package com.github.kzhunmax.jobsearch.user.controller;

import com.github.kzhunmax.jobsearch.payload.ApiResponse;
import com.github.kzhunmax.jobsearch.security.UserDetailsImpl;
import com.github.kzhunmax.jobsearch.user.dto.ResumeSummaryDTO;
import com.github.kzhunmax.jobsearch.user.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/user/resume")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Resumes", description = "Endpoints for managing user resumes")
public class ResumeController {

    private final ResumeService resumeService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all resumes for the current user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Resumes retrieved successfully",
                    useReturnTypeSchema = true
            )
    })
    public ResponseEntity<ApiResponse<List<ResumeSummaryDTO>>> getAllResumes(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getId();
        log.info("Getting all resumes for user ID={}", userId);
        List<ResumeSummaryDTO> resumes = resumeService.getAllResumes(userId);
        log.info("Found {} resumes for user ID={}", resumes.size(), userId);
        return ApiResponse.success(resumes);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Add a new resume for the current user (max 2)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Resume added successfully",
                    useReturnTypeSchema = true
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid file or maximum resume limit reached",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<ResumeSummaryDTO>> addResume(
            @Parameter(description = "Resume file (PDF, max 5MB)", required = true)
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getId();
        log.info("Adding resume for user ID={}", userId);
        ResumeSummaryDTO newResume = resumeService.addResume(userId, file);
        log.info("Resume added successfully for user ID={} with ID={}", userId, newResume.id());
        return ApiResponse.created(newResume);
    }

    @PutMapping(value = "/{resumeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update an existing resume by ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Resume updated successfully",
                    useReturnTypeSchema = true
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid file",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied (not owner)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Resume not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<ResumeSummaryDTO>> updateResume(
            @Parameter(description = "ID of the resume to update", example = "1")
            @PathVariable Long resumeId,
            @Parameter(description = "New resume file (PDF, max 5MB)", required = true)
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getId();
        log.info("Updating resume ID={} for user ID={}", resumeId, userId);
        ResumeSummaryDTO updatedResume = resumeService.updateResume(resumeId, userId, file);
        log.info("Resume ID={} updated successfully for user ID={}", resumeId, userId);
        return ApiResponse.success(updatedResume);
    }

    @DeleteMapping(value = "/{resumeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete a resume by ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "Resume deleted successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied (not owner)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Resume not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Conflict (resume is in use by applications)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<Void>> deleteResume(
            @Parameter(description = "ID of the resume to delete", example = "1")
            @PathVariable Long resumeId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getId();
        log.info("Deleting resume ID={} for user ID={}", resumeId, userId);
        resumeService.deleteResume(resumeId, userId);
        log.info("Resume ID={} deleted successfully for user ID={}", resumeId, userId);
        return ApiResponse.noContent();
    }
}