package com.github.kzhunmax.jobsearch.job.service;

import com.github.kzhunmax.jobsearch.exception.ApplicationNotFoundException;
import com.github.kzhunmax.jobsearch.exception.DuplicateApplicationException;
import com.github.kzhunmax.jobsearch.exception.JobNotFoundException;
import com.github.kzhunmax.jobsearch.exception.UserProfileNotFound;
import com.github.kzhunmax.jobsearch.job.dto.JobApplicationResponseDTO;
import com.github.kzhunmax.jobsearch.job.mapper.JobApplicationMapper;
import com.github.kzhunmax.jobsearch.job.model.Job;
import com.github.kzhunmax.jobsearch.job.model.JobApplication;
import com.github.kzhunmax.jobsearch.job.repository.JobApplicationRepository;
import com.github.kzhunmax.jobsearch.job.repository.JobRepository;
import com.github.kzhunmax.jobsearch.shared.enums.ApplicationStatus;
import com.github.kzhunmax.jobsearch.user.model.Resume;
import com.github.kzhunmax.jobsearch.user.model.User;
import com.github.kzhunmax.jobsearch.user.model.UserProfile;
import com.github.kzhunmax.jobsearch.user.repository.ResumeRepository;
import com.github.kzhunmax.jobsearch.user.repository.UserProfileRepository;
import com.github.kzhunmax.jobsearch.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
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
    private final ResumeRepository resumeRepository;
    private final JobApplicationMapper jobApplicationMapper;
    private final UserProfileRepository userProfileRepository;
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
    public JobApplicationResponseDTO applyToJob(Long jobId, Long userId, String coverLetter, MultipartFile resumeFile) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Applying to job - jobId={}, userId={}", requestId, jobId, userId);
        Job job = findJobById(jobId);
        User candidate = findUserById(userId);
        UserProfile candidateProfile = findUserProfileByUserId(userId);
        validateNoDuplicateApplication(job, candidate);
        validateResume(resumeFile);
        String resumeUrl = uploadCvToSupabase(resumeFile, userId, requestId);
        Resume resume = createAndSaveResume(resumeFile, resumeUrl, candidateProfile);
        JobApplication application = createAndSaveApplication(job, candidate, coverLetter, resume);
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

    @Cacheable(value = "applicationByCandidate", key = "{#userId, #pageable}")
    @Transactional(readOnly = true)
    public PagedModel<EntityModel<JobApplicationResponseDTO>> getApplicationsByCandidate(
            Long userId,
            Pageable pageable,
            PagedResourcesAssembler<JobApplicationResponseDTO> pagedAssembler
    ) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Fetching application by candidate - userId={}, pageable={}", requestId, userId, pageable);
        User candidate = findUserById(userId);
        Page<JobApplicationResponseDTO> applicationPage = jobApplicationRepository.findByCandidate(candidate, pageable).map(jobApplicationMapper::toDto);
        long total = applicationPage.getTotalPages();
        log.info("Request [{}]: Found {} applications for candidate - userId={}", requestId, total, userId);
        return pagedAssembler.toModel(applicationPage, EntityModel::of);
    }

    private Job findJobById(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException(jobId));
    }

    private UserProfile findUserProfileByUserId(Long userId) {
        return userProfileRepository.findById(userId)
                .orElseThrow(() -> new UserProfileNotFound(userId));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
    }

    private JobApplication findApplicationById(Long appId) {
        return jobApplicationRepository.findById(appId)
                .orElseThrow(() -> new ApplicationNotFoundException(appId));
    }

    private void validateNoDuplicateApplication(Job job, User candidate) {
        if (jobApplicationRepository.findByJobAndCandidate(job, candidate).isPresent())
            throw new DuplicateApplicationException();
    }

    private Resume createAndSaveResume(MultipartFile resumeFile, String resumeUrl, UserProfile userProfile) {
        Resume resume = Resume.builder()
                .title(resumeFile.getOriginalFilename())
                .fileUrl(resumeUrl)
                .userProfile(userProfile)
                .build();
        return resumeRepository.save(resume);
    }

    private JobApplication createAndSaveApplication(Job job, User candidate, String coverLetter, Resume resumeFile) {
        JobApplication application = JobApplication.builder()
                .job(job)
                .candidate(candidate)
                .status(ApplicationStatus.APPLIED)
                .coverLetter(coverLetter)
                .resume(resumeFile)
                .build();

        return jobApplicationRepository.save(application);
    }

    private void validateResume(MultipartFile resumeFile) {
        if (resumeFile.isEmpty() || !Objects.equals(resumeFile.getContentType(), "application/pdf")) {
            throw new IllegalArgumentException("CV must be a non-empty PDF file");
        }
        if (resumeFile.getSize() > ACCEPTABLE_FILE_SIZE) {
            throw new IllegalArgumentException("CV size exceeds 5MB limit");
        }
    }

    private String uploadCvToSupabase(MultipartFile cv, Long userId, String requestId) {
        try {
            String uuid = UUID.randomUUID().toString();
            String originalName = StringUtils.cleanPath(Objects.requireNonNull(cv.getOriginalFilename()));
            String fileName = uuid + "_" + StringUtils.getFilename(originalName);
            String path = "candidates/" + userId + "/" + fileName;

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
            log.error("Request [{}]: Failed to upload CV to Supabase S3 for userId={}", requestId, userId, e);
            throw new RuntimeException("CV upload to Supabase S3 failed: " + e.getMessage());
        }
    }
}
