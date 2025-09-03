package com.github.kzhunmax.jobsearch.controller;

import com.github.kzhunmax.jobsearch.dto.request.JobRequestDTO;
import com.github.kzhunmax.jobsearch.dto.response.JobResponseDTO;
import com.github.kzhunmax.jobsearch.payload.ApiResponse;
import com.github.kzhunmax.jobsearch.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {
    private final JobService jobService;

    @PreAuthorize("hasRole('RECRUITER')")
    @PostMapping
    public ResponseEntity<ApiResponse<JobResponseDTO>> createJob(
            @Valid @RequestBody JobRequestDTO dto,
            Authentication authentication
    ) {
        String username = authentication.getName();
        JobResponseDTO job = jobService.createJob(dto, username);
        return ApiResponse.created(job, MDC.get("requestId"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<JobResponseDTO>>> listJobs(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<JobResponseDTO> jobs = jobService.getAllActiveJobs(pageable);
        return ApiResponse.success(jobs, MDC.get("requestId"));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<ApiResponse<JobResponseDTO>> getJobById(@PathVariable Long jobId) {
        JobResponseDTO job = jobService.getJobById(jobId);
        return ApiResponse.success(job, MDC.get("requestId"));
    }

    @PreAuthorize("@jobSecurityService.isJobOwner(#jobId, authentication)")
    @PutMapping("/{jobId}")
    public ResponseEntity<ApiResponse<JobResponseDTO>> updateJob(
            @PathVariable Long jobId,
            @Valid @RequestBody JobRequestDTO dto) {
        JobResponseDTO updatedJob = jobService.updateJob(jobId, dto);
        return ApiResponse.success(updatedJob, MDC.get("requestId"));
    }

    @PreAuthorize("@jobSecurityService.isJobOwner(#jobId, authentication)")
    @DeleteMapping("/{jobId}")
    public ResponseEntity<ApiResponse<Void>> deleteJob(@PathVariable Long jobId) {
        jobService.deleteJob(jobId);
        return ApiResponse.noContent(MDC.get("requestId"));
    }

//    @GetMapping("/search")
//    public ResponseEntity<ApiResponse<Page<JobResponseDTO>>> searchJobs(
//            @RequestParam(required = false) String title,
//            @RequestParam(required = false) String company,
//            @RequestParam(required = false) String location,
//            @RequestParam(required = false) Double minSalary,
//            @RequestParam(required = false) Double maxSalary,
//            @PageableDefault(size = 20) Pageable pageable) {
//        Page<JobResponseDTO> results = jobService.searchJobs(title, company, location, minSalary, maxSalary, pageable);
//        return ApiResponse.success(results, MDC.get("requestId"));
//    }

    @PreAuthorize("hasRole('RECRUITER')")
    @GetMapping("/my-jobs")
    public ResponseEntity<ApiResponse<Page<JobResponseDTO>>> getMyJobs(
            Authentication authentication,
            @PageableDefault(size = 20) Pageable pageable) {
        String username = authentication.getName();
        Page<JobResponseDTO> jobs = jobService.getJobsByRecruiter(username, pageable);
        return ApiResponse.success(jobs, MDC.get("requestId"));
    }
}
