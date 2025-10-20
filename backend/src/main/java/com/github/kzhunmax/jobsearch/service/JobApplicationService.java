package com.github.kzhunmax.jobsearch.service;

import com.github.kzhunmax.jobsearch.dto.response.JobApplicationResponseDTO;
import com.github.kzhunmax.jobsearch.exception.ApplicationNotFoundException;
import com.github.kzhunmax.jobsearch.exception.DuplicateApplicationException;
import com.github.kzhunmax.jobsearch.exception.JobNotFoundException;
import com.github.kzhunmax.jobsearch.mapper.JobApplicationMapper;
import com.github.kzhunmax.jobsearch.model.ApplicationStatus;
import com.github.kzhunmax.jobsearch.model.Job;
import com.github.kzhunmax.jobsearch.model.JobApplication;
import com.github.kzhunmax.jobsearch.model.User;
import com.github.kzhunmax.jobsearch.repository.JobApplicationRepository;
import com.github.kzhunmax.jobsearch.repository.JobRepository;
import com.github.kzhunmax.jobsearch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.Objects;
import java.util.UUID;

import static com.github.kzhunmax.jobsearch.constants.LoggingConstants.REQUEST_ID_MDC_KEY;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobApplicationService {
    private final JobApplicationRepository jobApplicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JobApplicationMapper jobApplicationMapper;
    private final S3Client s3Client;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.bucket:resumes}")
    private String supabaseBucket;

    private static final int ACCEPTABLE_FILE_SIZE = 5 * 1024 * 1024;

    @Caching(evict = {
            @CacheEvict(value = "applicationByJob", allEntries = true),
            @CacheEvict(value = "applicationByCandidate", allEntries = true)
    })
    @Transactional
    public JobApplicationResponseDTO applyToJob(Long jobId, String email, String coverLetter, MultipartFile cv) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Applying to job - jobId={}, email={}", requestId, jobId, email);
        Job job = findJobById(jobId);
        User candidate = findUserByEmail(email);
        validateNoDuplicateApplication(job, candidate);
        validateResume(cv);
        String cvUrl = uploadCvToSupabase(cv, email, requestId);
        JobApplication application = createAndSaveApplication(job, candidate, coverLetter, cvUrl);
        log.info("Request [{}]: Application saved successfully - applicationId={}, jobId={}", requestId, application.getId(), jobId);
        return jobApplicationMapper.toDto(application);
    }

    @Cacheable(value = "applicationByJob", key = "{#jobId, #pageable}")
    @Transactional(readOnly = true)
    public PagedModel<EntityModel<JobApplicationResponseDTO>> getApplicationsForJob(
            Long jobId,
            Pageable pageable,
            PagedResourcesAssembler<JobApplicationResponseDTO> pagedAssembler
    ) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Fetching applications for jobId={} | pageable={}", requestId, jobId, pageable);
        Job job = findJobById(jobId);

        Page<JobApplicationResponseDTO> applicationPage = jobApplicationRepository
                .findByJob(job, pageable)
                .map(jobApplicationMapper::toDto);

        long total = applicationPage.getTotalElements();
        log.info("Request [{}]: Found {} applications for job - jobId={}", requestId, total, jobId);
        return pagedAssembler.toModel(applicationPage, EntityModel::of);
    }

    @Caching(evict = {
            @CacheEvict(value = "applicationByJob", allEntries = true),
            @CacheEvict(value = "applicationByCandidate", allEntries = true)
    })
    @Transactional
    public JobApplicationResponseDTO updateApplicationStatus(Long appId, ApplicationStatus status) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Updating application status - appId={}", requestId, appId);
        JobApplication application = findApplicationById(appId);
        application.setStatus(status);
        JobApplication savedApplication = jobApplicationRepository.save(application);
        log.info("Request [{}]: Application status updated successfully - appId={}", requestId, appId);
        return jobApplicationMapper.toDto(savedApplication);
    }

    @Cacheable(value = "applicationByCandidate", key = "{#email, #pageable}")
    @Transactional(readOnly = true)
    public PagedModel<EntityModel<JobApplicationResponseDTO>> getApplicationsByCandidate(
            String email,
            Pageable pageable,
            PagedResourcesAssembler<JobApplicationResponseDTO> pagedAssembler
    ) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Fetching application by candidate - email={}, pageable={}", requestId, email, pageable);
        User candidate = findUserByEmail(email);
        Page<JobApplicationResponseDTO> applicationPage = jobApplicationRepository.findByCandidate(candidate, pageable).map(jobApplicationMapper::toDto);
        long total = applicationPage.getTotalPages();
        log.info("Request [{}]: Found {} applications for candidate - email={}", requestId, total, email);
        return pagedAssembler.toModel(applicationPage, EntityModel::of);
    }

    private Job findJobById(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException(jobId));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));
    }

    private JobApplication findApplicationById(Long appId) {
        return jobApplicationRepository.findById(appId)
                .orElseThrow(() -> new ApplicationNotFoundException(appId));
    }

    private void validateNoDuplicateApplication(Job job, User candidate) {
        if (jobApplicationRepository.findByJobAndCandidate(job, candidate).isPresent())
            throw new DuplicateApplicationException();
    }

    private JobApplication createAndSaveApplication(Job job, User candidate, String coverLetter, String cvUrl) {
        JobApplication application = JobApplication.builder()
                .job(job)
                .candidate(candidate)
                .status(ApplicationStatus.APPLIED)
                .coverLetter(coverLetter)
                .cvUrl(cvUrl)
                .build();

        return jobApplicationRepository.save(application);
    }

    private void validateResume(MultipartFile cv) {
        if (cv.isEmpty() || !Objects.equals(cv.getContentType(), "application/pdf")) {
            throw new IllegalArgumentException("CV must be a non-empty PDF file");
        }
        if (cv.getSize() > ACCEPTABLE_FILE_SIZE) {
            throw new IllegalArgumentException("CV size exceeds 5MB limit");
        }
    }

    private String uploadCvToSupabase(MultipartFile cv, String email, String requestId) {
        try {
            String uuid = UUID.randomUUID().toString();
            String originalName = StringUtils.cleanPath(Objects.requireNonNull(cv.getOriginalFilename()));
            String fileName = uuid + "_" + StringUtils.getFilename(originalName);
            String path = "candidates/" + email.replace('@', '_') + "/" + fileName;

            log.debug("Request [{}]: Uploading CV to Supabase S3 at path={}", requestId, path);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(supabaseBucket)
                    .key(path)
                    .contentType(cv.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(
                    cv.getInputStream(), cv.getSize()));

            String publicUrl = supabaseUrl + "/storage/v1/object/public/" + supabaseBucket + "/" + path;

            log.info("Request [{}]: CV uploaded successfully to Supabase S3 - url={}", requestId, publicUrl);
            return publicUrl;

        } catch (Exception e) {
            log.error("Request [{}]: Failed to upload CV to Supabase S3 for email={}", requestId, email, e);
            throw new RuntimeException("CV upload to Supabase S3 failed: " + e.getMessage());
        }
    }
}
