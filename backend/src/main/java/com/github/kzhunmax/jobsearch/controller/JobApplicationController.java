package com.github.kzhunmax.jobsearch.controller;

import com.github.kzhunmax.jobsearch.dto.response.JobApplicationResponseDTO;
import com.github.kzhunmax.jobsearch.model.ApplicationStatus;
import com.github.kzhunmax.jobsearch.service.JobApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
public class JobApplicationController {
    private final JobApplicationService jobApplicationService;

    @PostMapping("/apply/{jobId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<JobApplicationResponseDTO> apply(@PathVariable Long jobId, Authentication authentication) {
        String username = authentication.getName();
        JobApplicationResponseDTO dto = jobApplicationService.applyToJob(jobId, username);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/job/{jobId}")
    @PreAuthorize("@jobSecurityService.isJobOwner(#jobId, authentication) or hasRole('ADMIN')")
    public ResponseEntity<PagedModel<EntityModel<JobApplicationResponseDTO>>> getApplicationForJob(@PathVariable Long jobId, Pageable pageable) {
        return ResponseEntity.ok(jobApplicationService.getApplicationsForJob(jobId, pageable));
    }

    @GetMapping("/my-applications")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PagedModel<EntityModel<JobApplicationResponseDTO>>> getMyApplications(Authentication authentication, Pageable pageable) {
        String username = authentication.getName();
        return ResponseEntity.ok(jobApplicationService.getApplicationsByCandidate(username, pageable));
    }

    @PatchMapping("/{appId}/status")
    @PreAuthorize("@jobSecurityService.canUpdateApplication(#appId, #status, authentication)")
    public ResponseEntity<JobApplicationResponseDTO> updateStatus(@PathVariable Long appId, @RequestParam @Valid ApplicationStatus status) {
        return ResponseEntity.ok(jobApplicationService.updateApplicationStatus(appId, status));
    }
}
